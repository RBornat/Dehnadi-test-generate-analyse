package uk.ac.mdx.RBornat.Saeedgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Decoder extends JPanel 
                     implements KeyListener {
    //Values for the fields

    private final int rows = 4;
    private final JLabel[][] labels;
    private final JTextField[][] numfields;
    
    Decoder (String otherString, String response, State s) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        labels = new JLabel[s.size()][rows];
        numfields = new JTextField[s.size()][rows];
        
        JPanel bigPanel = new JPanel(new GridLayout(rows,s.size()));
        
        //Create the labels and fields, each pair in a panel, each row in a panel
        for (int j=0; j<rows; j++) {
            JPanel rowPanel = new JPanel();
            for (int i=0; i<s.size(); i++) {
                String text = s.item(i).var.toString()+"=";
                JLabel label = new JLabel(text);
                labels[i][j] = label;

                JTextField field = new JTextField(); 
                field.addKeyListener(this); 
                field.setColumns(4);
                numfields[i][j] = field;

                label.setLabelFor(field);

                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                panel.add(label, BorderLayout.WEST);
                panel.add(field, BorderLayout.EAST);
                
                rowPanel.add(panel);
            }
            bigPanel.add(rowPanel);
        }

        add(new JLabel("in response to \""+otherString+"\""));
        add(new JLabel("the subject typed \""+response+"\""));
        add(new JLabel("can you decode?"));
        add((Box.createRigidArea(new Dimension(0,5))));
        add(bigPanel);
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!((c >= '0') && (c <= '9') ||
                (c == KeyEvent.VK_BACK_SPACE) ||
                (c == KeyEvent.VK_DELETE))) {
            getToolkit().beep();
            e.consume();
        }
    }
    
    public void keyPressed(KeyEvent e) {
        return;
    }

    public void keyReleased(KeyEvent e) {
        return;
    }

    /*
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    static SimpleSet<State> showDecoder(final String otherString, final String response, final State s) {
        Decoder d = new Decoder(otherString, response, s);
        int reply = JOptionPane.showOptionDialog(null, d, 
                "please decode", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);

        SimpleSet<State> ss = new SimpleSet<State>();
        if (reply==JOptionPane.OK_OPTION) {
            for (int j=0; j<d.rows; j++) {
                State s1 = new State();
                for (int i=0; i<s.size(); i++) {
                    String num = d.numfields[i][j].getText();
                    if (num.equals("")) {
                        s1 = null; break;
                    }
                    else
                        s1 = s1.add(s.item(i).var, new Value(Integer.parseInt(num)));
                }
                if (s1!=null)
                    ss.add(s1);
            }
            return ss;
        }
        else
            return null;
    }
}
