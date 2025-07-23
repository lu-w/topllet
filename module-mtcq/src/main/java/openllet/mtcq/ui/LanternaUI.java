package openllet.mtcq.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.ansi.UnixLikeTerminal;
import com.googlecode.lanterna.terminal.ansi.UnixTerminal;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.gui2.*;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.results.QueryResult;
import openllet.query.sparqldl.model.results.ResultBinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;

public class LanternaUI implements MTCQEngineUI {

    private Screen _screen;
    private Window _window;
    private MultiWindowTextGUI _gui;
    private final Map<MetricTemporalConjunctiveQuery, QueryResult> _resultsToPrintInStreamingMode = new HashMap<>();

    @Override
    public void setup(MetricTemporalConjunctiveQuery query) {
        try
        {
            Terminal _terminal = new UnixTerminal(System.in, System.out, StandardCharsets.UTF_8,
                    UnixLikeTerminal.CtrlCBehaviour.CTRL_C_KILLS_APPLICATION);
            _screen = new TerminalScreen(_terminal);
            _screen.startScreen();
            _window = new BasicWindow();
            _gui = new MultiWindowTextGUI(_screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            _gui.addWindow(_window);
            _screen.doResizeIfNecessary();
            _gui.getGUIThread().processEventsAndUpdate();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public void tearDown() {
        try
        {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void informAboutStartOfIteration(int timePoint) {

    }

    @Override
    public void informAboutEndOfIteration(int timePoint) {
        _resultsToPrintInStreamingMode.clear();
    }

    @Override
    public void informAboutResults(int timePoint, KnowledgeBase kb, Query<?> query, QueryResult result) {
        if (result != null && query instanceof MetricTemporalConjunctiveQuery q) {
            _resultsToPrintInStreamingMode.put(q, result);
            refresh(timePoint, kb);
        }
    }

    private void refresh(int t, KnowledgeBase kb)
    {
        Panel panel = new Panel();
        panel.setSize(_screen.getTerminalSize());
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        _window.setComponent(panel);
        Label toplletLabel = new Label("Topllet Stream Reasoner");
        panel.addComponent(toplletLabel);

        String aboxText = printAboxInfo(kb);
        Panel aboxP = new Panel();
        aboxP.setSize(_screen.getTerminalSize());
        aboxP.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        Label aboxLabel = new Label("");
        aboxLabel.setLabelWidth(150);
        aboxLabel.setText(aboxText);
        Label aboxHead = new Label("");
        aboxHead.setLabelWidth(150);
        aboxHead.setText("ABox Stats (t = " + t + "):");
        aboxP.addComponent(aboxHead);
        aboxP.addComponent(aboxLabel);
        panel.addComponent(aboxP.withBorder(Borders.doubleLine()));

        List<MetricTemporalConjunctiveQuery> queries = new ArrayList<>(_resultsToPrintInStreamingMode.keySet().stream().toList());
        queries.sort(Comparator.comparing(Object::toString));
        for (MetricTemporalConjunctiveQuery q : queries) {
            String text = printSimpleResult(_resultsToPrintInStreamingMode.get(q));
            Panel p = new Panel();
            p.setSize(_screen.getTerminalSize());
            p.setLayoutManager(new LinearLayout(Direction.VERTICAL));
            Label lr = new Label("");
            lr.setLabelWidth(150);
            lr.setText(text);
            Label le = new Label("");
            le.setLabelWidth(150);
            Label lq = new Label("");
            lq.setLabelWidth(150);
            lq.setText(q.toString());
            p.addComponent(lq);
            p.addComponent(le);
            p.addComponent(lr);
            panel.addComponent(p.withBorder(Borders.doubleLine()));
        }
        try {
            _screen.doResizeIfNecessary();
            _gui.getGUIThread().processEventsAndUpdate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String printSimpleResult(QueryResult resultBindings)
    {
        StringBuilder res = new StringBuilder("{");
        for (ResultBinding binding : resultBindings)
        {
            res.append("(");
            for (ATermAppl var : resultBindings.getQuery().getResultVars())
                res.append(binding.getValue(var)).append(", ");
            if (res.length() > 2)
                res.delete(res.length() - 2, res.length());
            res.append("), ");
        }
        if (res.length() > 2)
            res.delete(res.length() - 2, res.length());
        res.append("}");
        return res.toString();
    }

    private String printAboxInfo(KnowledgeBase kb)
    {
        int relCount = 0;
        int clsCount = 0;
        int indCount = 0;
        int litCount = 0;
        for (ATermAppl i : kb.getIndividuals())
        {
            indCount++;
            clsCount += max(0, kb.getABox().getIndividual(i).getTypes().size() - 2); // -2 due to _TOP_ and FunValue(i)
            relCount += kb.getABox().getIndividual(i).getOutEdges().size();
        }
        for (ATermAppl n : kb.getABox().getNodeList())
            if (kb.getABox().getLiteral(n) != null)
                litCount++;
        return "Individuals: " + indCount + "\nLiterals: " + litCount + "\nTypes: " + clsCount + "\nRelations: " +
                relCount + "\n";
    }
}
