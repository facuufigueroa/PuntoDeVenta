package View;

import Backup.BackupService;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Future;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class BackupView extends JFrame {
    private final BackupService service;
    private final JTextField directory = new JTextField();
    private final JTextField executable = new JTextField();
    private final JCheckBox automatic = new JCheckBox("Backup automático cada lunes");
    private final JTextArea status = new JTextArea(3, 30);
    private final JLabel lastCopy = new JLabel();
    private final JButton backup = new JButton();
    private final JButton save = new JButton();
    private final Timer refresh;

    public BackupView(BackupService service) {
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = Estilo.shell(this, "Copias de seguridad", "Protegé los productos y los precios de tu almacén.");
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        automatic.setOpaque(false);
        automatic.setSelected(Boolean.parseBoolean(service.get("enabled")));
        automatic.setFont(new Font("Segoe UI", Font.BOLD, 16));
        automatic.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(automatic);
        form.add(Box.createVerticalStrut(14));
        addRow(form, "Carpeta donde se guardan las copias", directory, false);
        addRow(form, "Programa mysqldump de MySQL", executable, true);
        directory.setText(service.get("directory"));
        executable.setText(service.get("executable"));
        JTextArea explanation = new JTextArea("Si un lunes está cerrado o la PC está apagada, la copia pendiente se hace al volver a abrir el sistema. Si falla, se reintenta cada 30 minutos mientras el sistema está abierto.");
        explanation.setLineWrap(true); explanation.setWrapStyleWord(true);
        explanation.setEditable(false); explanation.setFocusable(false);
        explanation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        explanation.setForeground(new Color(100, 116, 109));
        explanation.setRows(3);
        explanation.setPreferredSize(new Dimension(620, 72));
        explanation.setMinimumSize(new Dimension(0, 72));
        explanation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(explanation);
        form.add(Box.createVerticalStrut(12));
        lastCopy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lastCopy.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lastCopy);
        form.add(Box.createVerticalStrut(12));
        status.setLineWrap(true); status.setWrapStyleWord(true);
        status.setEditable(false); status.setFocusable(false);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        status.setForeground(new Color(22, 112, 80));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        status.setMinimumSize(new Dimension(0, 64));
        status.setPreferredSize(new Dimension(620, 64));
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        form.add(status);
        form.add(Box.createVerticalGlue());
        root.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(Estilo.button(save, "Guardar configuración", false));
        backup.setIcon(Estilo.imagen("paquete.png", 22, 22));
        actions.add(Estilo.button(backup, "Hacer backup ahora", true));
        root.add(actions, BorderLayout.SOUTH);
        save.addActionListener(event -> persist());
        backup.addActionListener(event -> manual());
        refresh = new Timer(1000, event -> refresh());
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) { refresh.start(); }
            @Override public void windowClosed(WindowEvent event) { refresh.stop(); }
        });
        refresh();
        setSize(790, 650);
        setMinimumSize(new Dimension(740, 650));
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel form, String title, JTextField field, boolean file) {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
        panel.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(label, BorderLayout.NORTH);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(400, 38));
        panel.add(field, BorderLayout.CENTER);
        JButton choose = Estilo.button(new JButton(), "Elegir…", false);
        panel.add(choose, BorderLayout.EAST);
        choose.addActionListener(event -> {
            JFileChooser picker = new JFileChooser();
            picker.setFileSelectionMode(file ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
            if (!field.getText().trim().isEmpty()) picker.setSelectedFile(new File(field.getText()));
            if (picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                field.setText(picker.getSelectedFile().getAbsolutePath());
        });
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        panel.setPreferredSize(new Dimension(620, 76));
        form.add(panel);
        form.add(Box.createVerticalStrut(12));
    }

    private boolean persist() {
        try {
            service.configure(directory.getText(), executable.getText(), automatic.isSelected());
            status.setText("Configuración guardada.");
            return true;
        } catch (Exception error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void manual() {
        if (!persist()) return;
        backup.setEnabled(false);
        save.setEnabled(false);
        status.setText("Preparando copia de seguridad…");
        Future<Path> result = service.manual();
        new SwingWorker<Path, Void>() {
            @Override protected Path doInBackground() throws Exception { return result.get(); }
            @Override protected void done() {
                backup.setEnabled(true); save.setEnabled(true);
                try {
                    Path file = get();
                    JOptionPane.showMessageDialog(BackupView.this, "Copia guardada en:\n" + file,
                            "Backup completado", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception error) {
                    JOptionPane.showMessageDialog(BackupView.this, service.getStatus(),
                            "No se pudo hacer el backup", JOptionPane.ERROR_MESSAGE);
                }
                refresh();
            }
        }.execute();
    }

    private void refresh() {
        String last = service.get("lastSuccess");
        lastCopy.setText(last.isEmpty() ? "Todavía no hay una copia completada." : "Última copia: " + last);
        lastCopy.setToolTipText(service.get("lastFile"));
        status.setText(service.getStatus());
    }
}
