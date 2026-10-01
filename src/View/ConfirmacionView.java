package View;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Modal confirmation with an explicit default button and keyboard cancellation. */
public final class ConfirmacionView extends JDialog {
    private boolean aceptado;

    public ConfirmacionView(JFrame owner, String title, String message, String acceptText) {
        super(owner, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(28, 28, 24, 28));
        JLabel badge = new JLabel("BRU-YEN  /  CONFIRMACIÓN");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setForeground(new Color(22, 112, 80));
        badge.setIcon(Estilo.imagen("logo venta.png", 60, 36));
        badge.setIconTextGap(12);
        content.add(badge, BorderLayout.NORTH);
        JPanel text = new JPanel(new BorderLayout(0, 12));
        text.setOpaque(false);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 26));
        heading.setForeground(new Color(31, 47, 42));
        text.add(heading, BorderLayout.NORTH);
        JTextArea description = new JTextArea(message);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        description.setForeground(new Color(100, 116, 109));
        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setOpaque(false);
        description.setPreferredSize(new Dimension(420, 68));
        text.add(description, BorderLayout.CENTER);
        content.add(text, BorderLayout.CENTER);
        JButton cancel = Estilo.button(new JButton(), "Cancelar · Esc", false);
        JButton accept = Estilo.button(new JButton(), acceptText + " · Enter", true);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(cancel);
        footer.add(accept);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);
        accept.addActionListener(event -> { aceptado = true; dispose(); });
        cancel.addActionListener(event -> dispose());
        getRootPane().setDefaultButton(accept);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("pressed ENTER"), "confirmar");
        getRootPane().getActionMap().put("confirmar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                if (getFocusOwner() == cancel) cancel.doClick(0);
                else accept.doClick(0);
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "cancelar");
        getRootPane().getActionMap().put("cancelar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { cancel.doClick(0); }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) { accept.requestFocusInWindow(); }
        });
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isAceptado() { return aceptado; }

    public static boolean confirmar(JFrame owner, String title, String message, String acceptText) {
        ConfirmacionView dialog = new ConfirmacionView(owner, title, message, acceptText);
        dialog.setVisible(true);
        return dialog.isAceptado();
    }
}
