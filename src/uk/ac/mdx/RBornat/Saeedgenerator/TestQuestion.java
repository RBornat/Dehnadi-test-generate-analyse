package uk.ac.mdx.RBornat.Saeedgenerator;

public class TestQuestion {
    final State state;
    final Assign[] commands;
    
    TestQuestion(State s, Assign[] commands) {
        this.state = s; this.commands = commands;
    }
    
    // two vars, one assign
    TestQuestion(Variable a, Value va, Variable b, Value vb, Assign command) {
        this(new State(a, va, b, vb), new Assign[]{command});
    }
    
    // two vars, two assigns
    TestQuestion(Variable a, Value va, Variable b, Value vb, 
                Assign command1, Assign command2) {
        this(new State(a, va, b, vb), new Assign[]{command1,command2});
    }

    // three vars, two assigns
    TestQuestion(Variable a, Value va, Variable b, Value vb, Variable c, Value vc, 
                Assign command1, Assign command2) {
        this(new State(a, va, b, vb, c, vc), new Assign[]{command1,command2});
    }
    
    // three vars, three assigns
    TestQuestion(Variable a, Value va, Variable b, Value vb, Variable c, Value vc, 
                Assign command1, Assign command2, Assign command3) {
        this(new State(a, va, b, vb, c, vc), new Assign[]{command1,command2,command3});
    }
}
