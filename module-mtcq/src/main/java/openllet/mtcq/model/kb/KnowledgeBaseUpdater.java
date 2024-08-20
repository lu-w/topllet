package openllet.mtcq.model.kb;

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

public class KnowledgeBaseUpdater
{
    public static final Logger _logger = Log.getLogger(KnowledgeBaseUpdater.class);

    private final KnowledgeBase _kb;
    private final HashMap<String, String> _prefixes = new HashMap<>();
    private boolean _isLast = false;
    private final ZMQ.Socket _socket;
    private Timer _timer = null;

    public KnowledgeBaseUpdater(KnowledgeBase kb)
    {
        this._kb = kb;
        _socket = new ZContext().createSocket(SocketType.PULL);
        _socket.bind("tcp://localhost:5555");
    }

    public KnowledgeBaseUpdater(KnowledgeBase kb, Timer timer)
    {
        this(kb);
        _timer = timer;
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
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.startsWith("#"))
            {
                if (line.startsWith("PREFIX"))
                    addPrefix(line.substring(6));
                else if (line.startsWith("DELETE"))
                    applyDelete(parseData(line.substring(7)));
                else if (line.startsWith("ADD"))
                    applyAdd(parseData(line.substring(4)));
                else if (line.startsWith("UPDATE"))
                    applyUpdate(parseData(line.substring(7)));
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
                validateProperty(dataList.get(0));
                String[] inds = value.split(",");
                dataList.add(ATermUtils.makeTermAppl(replacePrefix(inds[0].trim())));
                dataList.add(ATermUtils.makeTermAppl(replacePrefix(inds[1].trim())));
            }
            else
            {
                validateConcept(dataList.get(0));
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

    private void applyDelete(List<ATermAppl> data)
    {
        if (data.size() == 2)
            _kb.removeType(data.get(1), data.get(0));
        else if (data.size() == 3)
            _kb.removePropertyValue(data.get(0), data.get(1), data.get(2));
        else
            throw new RuntimeException("Invalid data: " + data);
    }

    private void applyAdd(List<ATermAppl> data)
    {
        if (data.size() == 2)
        {
            if (!_kb.isIndividual(data.get(1)))
                _kb.addIndividual(data.get(1));
            _kb.addType(data.get(1), data.get(0));
        }
        else if (data.size() == 3)
        {
            if (!_kb.isIndividual(data.get(1)))
                _kb.addIndividual(data.get(1));
            if (!_kb.isIndividual(data.get(2)))
                _kb.addIndividual(data.get(2));
            _kb.addPropertyValue(data.get(0), data.get(1), data.get(2));
        }
        else
            throw new RuntimeException("Invalid data: " + data);
    }

    /**
     * Note: Updates are only defined on properties (exchanging *all* prior objects with the single new object).
     * @param data List of property, subject, object.
     */
    private void applyUpdate(List<ATermAppl> data)
    {
        if (data.size() == 3)
        {
            for (ATermAppl i2 : _kb.getPropertyValues(data.get(0), data.get(1)))
                _kb.removePropertyValue(data.get(0), data.get(1), i2);
            _kb.addPropertyValue(data.get(0), data.get(1), data.get(2));
        }
        else
            throw new RuntimeException("Invalid data: " + data);
    }
}
