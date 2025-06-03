package openllet.mtcq.model.kb;

import openllet.core.boxes.abox.Node;
import openllet.core.datatypes.Datatypes;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.shared.tools.Log;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import org.zeromq.SocketType;
import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Timer;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static openllet.core.utils.TermFactory.literal;

public class StreamingDataHandler
{
    public static final Logger _logger = Log.getLogger(StreamingDataHandler.class);

    private final KnowledgeBase _kb;
    private final HashMap<String, String> _prefixes = new HashMap<>();
    private final List<Map<String, ATermAppl>> anonClasses = new ArrayList<>();
    private boolean _isLast = false;
    private final ZMQ.Socket _socket;
    private Timer _timer = null;

    public StreamingDataHandler(KnowledgeBase kb, int port)
    {
        this._kb = kb;
        _socket = new ZContext().createSocket(SocketType.REP);
        _socket.bind("tcp://localhost:" + port);
    }

    public StreamingDataHandler(KnowledgeBase kb, int port, Timer timer)
    {
        this(kb, port);
        _timer = timer;
    }

    public void sendResult(QueryResult result)
    {
        _socket.send(result.toString());
    }

    public void sendAck()
    {
        _socket.send("ACK");
    }

    public void waitAndUpdateKB()
    {
        if (_timer != null)
            _timer.start();
        String message = _socket.recvStr();
        processMessage(message);
        if (_timer != null)
            _timer.stop();
    }

    public boolean isLast()
    {
        return _isLast;
    }

    /**
     * Messages are of the form:
     * [DELETE|ADD|UPDATE] [concept(ind)|role(ind1,ind2)]
     * Allows for prefixes of the form 'PREFIX myPrefix: <http://uri#>' at the beginning of the file
     * Allows for comments of the form '# my comment' for lines starting with '#'
     * @param message A message string as described above.
     */
    private void processMessage(String message)
    {
        message = message.trim();
        Scanner scanner = new Scanner(message);
        anonClasses.add(new HashMap<>());  // anon classes for current time point
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isBlank() && !line.startsWith("#"))
            {
                if (line.startsWith("PREFIX"))
                    addPrefix(line.substring(6));
                else if (line.startsWith("DELETE"))
                    deleteABoxAxiom(parseData(line.substring(7)));
                else if (line.startsWith("ADD"))
                {
                    List<ATermAppl> data = parseData(line.substring(4));
                    if (line.startsWith("ADD http://www.w3.org/2002/07/owl#Class"))
                        addClass(data);
                    else if (line.startsWith("ADD http://www.w3.org/2002/07/owl#Restriction"))
                    {
                        List<ATermAppl> onProperty = parseData(scanner.nextLine().trim().substring(4));
                        List<ATermAppl> restriction = parseData(scanner.nextLine().trim().substring(4));
                        if ("http://www.w3.org/2002/07/owl#someValuesFrom".equals(restriction.get(0).toString()))
                            addExistentialRestriction(onProperty, restriction);
                        else if ("http://www.w3.org/2002/07/owl#onClass".equals(restriction.get(0).toString()))
                        {
                            addQuantifiedRestriction(onProperty, restriction,
                                    parseData(scanner.nextLine().trim().substring(4)));
                        }
                        else
                            _logger.warning("Restriction " + restriction + " not yet supported. Ignoring.");

                    }
                    else if (line.startsWith("ADD http://www.w3.org/2002/07/owl#complementOf"))
                        addComplement(data);
                    else
                        addABoxAxiom(data);
                }
                else if (line.startsWith("UPDATE"))
                    updateABoxAxiom(parseData(line.substring(7)));
                else if (!line.equals("LAST"))
                    _logger.warning("Invalid knowledge base update: " + line);
            }
        }
        _isLast = message.endsWith("LAST");
    }

    private void addPrefix(String prefix)
    {
        String[] sides = prefix.trim().split(":", 2);
        if (sides.length == 2)
        {
            String rhs = sides[1].trim();
            if (rhs.startsWith("<"))
                rhs = rhs.substring(1);
            if (rhs.endsWith(">"))
                rhs = rhs.substring(0, rhs.length() - 1);
            _prefixes.put(sides[0].trim(), rhs);
        }
        else
            throw new IllegalArgumentException("Invalid prefix: " + prefix);
    }

    /**
     * Parses a data update string.
     * @param data A string of the form [DELETE|ADD|UPDATE] [concept(ind)|role(ind1,ind2)]
     * @return A list of ind, concept or role, ind1, ind2
     */
    private List<ATermAppl> parseData(String data)
    {
        List<ATermAppl> dataList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(.+)\\((.+)\\)");
        Matcher matcher = pattern.matcher(data);
        if (matcher.matches()) {
            dataList.add(ATermUtils.makeTermAppl(replacePrefix(matcher.group(1).trim())));
            String value = replacePrefix(matcher.group(2));
            if (value.contains(","))
            {
                //validateProperty(dataList.get(0));
                String[] inds = value.split(",");
                dataList.add(ATermUtils.makeTermAppl(replacePrefix(inds[0].trim())));
                dataList.add(ATermUtils.makeTermAppl(replacePrefix(inds[1].trim())));
            }
            else
            {
                //validateConcept(dataList.get(0));
                dataList.add(ATermUtils.makeTermAppl(replacePrefix(value.trim())));
            }
        }
        else
            throw new IllegalArgumentException("Invalid data: " + data);
        return dataList;
    }

    private String replacePrefix(String orig)
    {
        String replaced = orig;
        for (String prefix : _prefixes.keySet())
            if (replaced.startsWith(prefix + ":"))
            {
                replaced = replaced.replace(prefix + ":", _prefixes.get(prefix));
                break;
            }
        return replaced;
    }

    private void validateConcept(ATermAppl c)
    {
        if (!_kb.isClass(c))
            throw new IllegalArgumentException("Invalid concept: " + c);
    }

    private void validateProperty(ATermAppl p)
    {
        if (!_kb.isProperty(p))
            throw new IllegalArgumentException("Invalid property: " + p);
    }

    private void deleteABoxAxiom(List<ATermAppl> data)
    {
        if (data.size() == 2)
        {
            // If class was not found, it was one of the anonymous classes created at a previous time point
            // -> iterate over all anon classes created previously until we can be sure that no match is found
            boolean suc = false;
            ATermAppl cls;
            int i = anonClasses.size() - 1;
            do
            {
                cls = resolveClass(data.get(0), anonClasses.get(i), false);
                if (cls != null)
                    suc = _kb.removeType(data.get(1), cls);
                i--;
            }
            while (!suc && i >= 0);
            if (!suc)
                throw new RuntimeException("Can not delete data: " + data);
        }
        else if (data.size() == 3)
        {
            ATermAppl obj = getObjectInRole(data);
            boolean suc = _kb.removePropertyValue(data.get(0), data.get(1), obj);
            // Removes literal if nothing uses it anymore
            if (_kb.getRole(data.get(0)).isDatatypeRole())
            {
                Node objNode = _kb.getABox().getNode(obj);
                if (objNode != null && objNode.getInEdges().isEmpty())
                    _kb.getABox().removeNodeEntirely(obj);
                // TODO integer literals do not seem to be removed (objNode == null)
            }
            if (!suc)
                throw new RuntimeException("Can not delete data: " + data);
        }
        else
            throw new RuntimeException("Deleting ABox axiom for invalid data: " + data);
    }

    private void addClass(List<ATermAppl> data)
    {
        if (data.size() != 2)
            throw new RuntimeException("Adding class for invalid data: " + data);
        ATermAppl cls = resolveClass(data.get(1), anonClasses.get(anonClasses.size() - 1));
        if (_kb.getClasses().contains(cls))
            throw new RuntimeException("Adding new class that already exists: " + cls);
        _kb.addClass(cls);
    }

    private void addExistentialRestriction(List<ATermAppl> onProperty, List<ATermAppl> onClass)
    {
        if (onProperty.size() != 3 || onClass.size() != 3)
            throw new RuntimeException("Adding restriction for invalid data: " + onProperty + ", " + onClass);
        ATermAppl cls = resolveClass(onProperty.get(1), anonClasses.get(anonClasses.size() - 1));
        ATermAppl p = onProperty.get(2);
        ATermAppl forCls = resolveClass(onClass.get(1), anonClasses.get(anonClasses.size() - 1));
        if (!forCls.isEqual(cls))
            throw new RuntimeException("Restriction " + onClass + " on invalid class " + forCls +
                    " (expected " + cls + ")");
        ATermAppl onCls = resolveClass(onClass.get(2), anonClasses.get(anonClasses.size() - 1));
        _kb.addSubClass(cls, ATermUtils.makeSomeValues(p, onCls));
    }

    private void addQuantifiedRestriction(List<ATermAppl> onProperty, List<ATermAppl> onClass, List<ATermAppl> cardinality)
    {
        if (onProperty.size() != 3 || onClass.size() !=  3 || cardinality.size() != 3)
            throw new RuntimeException("Adding restriction for invalid data: " + onProperty + ", " + onClass);
        ATermAppl cls = resolveClass(onProperty.get(1), anonClasses.get(anonClasses.size() - 1));
        ATermAppl p = onProperty.get(2);
        int card = Integer.parseInt(cardinality.get(2).toString());
        ATermAppl forCls = resolveClass(onClass.get(1), anonClasses.get(anonClasses.size() - 1));
        if (!forCls.isEqual(cls))
            throw new RuntimeException("Restriction " + onClass + " on invalid class " + forCls +
                    " (expected " + cls + ")");
        ATermAppl onCls = resolveClass(onClass.get(2), anonClasses.get(anonClasses.size() - 1));
        _kb.addSubClass(cls, ATermUtils.makeCard(p, card, onCls));
    }

    private void addComplement(List<ATermAppl> data)
    {
        if (data.size() != 3)
            throw new RuntimeException("Adding restriction for invalid data: " + data);
        _kb.addComplementClass(resolveClass(data.get(1), anonClasses.get(anonClasses.size() - 1)),
                resolveClass(data.get(2), anonClasses.get(anonClasses.size() - 1)));
    }

    ATermAppl resolveClass(ATermAppl cls, Map<String, ATermAppl> anonClassIDsToClasses)
    {
        return resolveClass(cls, anonClassIDsToClasses, true);
    }

    ATermAppl resolveClass(ATermAppl cls, Map<String, ATermAppl> anonClassIDsToClasses, boolean createAnonClass)
    {
        if (anonClassIDsToClasses.containsKey(cls.toString()))
            cls = anonClassIDsToClasses.get(cls.toString());
        else if (cls.toString().startsWith("_:"))
        {
            if (createAnonClass)
            {
                String origName = cls.toString();
                do
                    cls = ATermUtils.makeTermAppl("_anon_" + (int) (Math.random() * 100000 + 1));
                while (_kb.getClasses().contains(cls));
                anonClassIDsToClasses.put(origName, cls);
            }
            else
                return null;
        }
        return cls;
    }

    private void addABoxAxiom(List<ATermAppl> data)
    {
        if (data.size() == 2)
        {
            ATermAppl ind = data.get(1);
            if (!_kb.isIndividual(ind))
                _kb.addIndividual(ind);
            ATermAppl cls = resolveClass(data.get(0), anonClasses.get(anonClasses.size() - 1));
            _kb.addType(ind, cls);
        }
        else if (data.size() == 3)
        {
            if (!_kb.getRole(data.get(0)).isDatatypeRole())
            {
                if (!_kb.isIndividual(data.get(1)))
                    _kb.addIndividual(data.get(1));
                if (!_kb.isIndividual(data.get(2)))
                    _kb.addIndividual(data.get(2));
            }
            _kb.addPropertyValue(data.get(0), data.get(1), getObjectInRole(data));
        }
        else
            throw new RuntimeException("Adding ABox axiom for invalid data: " + data);
    }

    private ATermAppl getObjectInRole(List<ATermAppl> data)
    {
        ATermAppl r = data.get(0);
        ATermAppl o = data.get(2);
        // Assumes type double for all datatype roles (this may be invalid)
        if (_kb.getRole(r).isDatatypeRole())
        {
            if (o.toString().contains("."))
                o = literal(o.toString(), Datatypes.DECIMAL);
            else
                o = literal(o.toString(), Datatypes.INTEGER);
        }
        return o;
    }

    /**
     * Note: Updates are only defined on properties (exchanging *all* prior objects with the single new object).
     * @param data List of property, subject, object.
     */
    private void updateABoxAxiom(List<ATermAppl> data)
    {
        if (data.size() == 3)
        {
            for (ATermAppl i2 : _kb.getPropertyValues(data.get(0), data.get(1)))
                _kb.removePropertyValue(data.get(0), data.get(1), i2);
            _kb.addPropertyValue(data.get(0), data.get(1), getObjectInRole(data));
        }
        else
            throw new RuntimeException("Updating ABox axiom for invalid data: " + data);
    }
}
