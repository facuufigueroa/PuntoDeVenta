package View;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/** Shared presentation layer; reuses the components wired to the controllers. */
public final class Estilo {
    private static final Color BACKGROUND = new Color(242, 245, 244);
    private static final Color INK = new Color(31, 47, 42);
    private static final Color GREEN = new Color(22, 112, 80);
    private static final Color MUTED = new Color(100, 116, 109);
    private static final Color LINE = new Color(219, 228, 223);

    private Estilo() {}

    private static final class Sidebar extends JPanel implements Scrollable {
        Sidebar() { super(new BorderLayout(0, 16)); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 16; }
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(16, visible.height - 16);
        }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }

    static ImageIcon imagen(String name, int width, int height) {
        java.net.URL resource = Estilo.class.getResource("/Imagenes/" + name);
        if (resource == null) return null;
        ImageIcon original = new ImageIcon(resource);
        double scale = Math.min((double) width / original.getIconWidth(),
                (double) height / original.getIconHeight());
        return new ImageIcon(original.getImage().getScaledInstance(
                Math.max(1, (int) Math.round(original.getIconWidth() * scale)),
                Math.max(1, (int) Math.round(original.getIconHeight() * scale)), Image.SCALE_SMOOTH));
    }

    private static Icon iconoBoton(Icon original, boolean primary) {
        if (original == null) return null;
        final Icon small = original instanceof ImageIcon
                ? new ImageIcon(((ImageIcon) original).getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH))
                : original;
        return new Icon() {
            public int getIconWidth() { return 28; }
            public int getIconHeight() { return 28; }
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    canvas.setColor(new Color(245, 250, 247));
                    canvas.fillRoundRect(x, y, 28, 28, 8, 8);
                }
                small.paintIcon(component, canvas, x + 3, y + 3);
                canvas.dispose();
            }
        };
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", size >= 18 ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(color);
        return label;
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JPanel row(Component... components) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);
        for (Component component : components) panel.add(component);
        return panel;
    }

    private static JPanel card(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(12, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(20, 20, 20, 20)));
        JPanel heading = column();
        JLabel cardTitle = label(title, 20, INK);
        String image = null;
        if (title.equals("Nueva venta") || title.equals("Carrito")) image = "carrito-de-compras.png";
        else if (title.equals("Productos") || title.equals("Catálogo") || title.equals("Datos del producto")) image = "paquete.png";
        else if (title.equals("Cobro")) image = "metodo-de-pago.png";
        else if (title.equals("Consultar precio") || title.equals("Consulta rápida")) image = "etiqueta-del-precio.png";
        if (image != null) {
            cardTitle.setIcon(imagen(image, 28, 28));
            cardTitle.setIconTextGap(10);
        }
        heading.add(cardTitle);
        if (subtitle != null) {
            heading.add(Box.createVerticalStrut(6));
            heading.add(label(subtitle, 13, MUTED));
        }
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    private static JPanel field(String title, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        panel.add(label(title, 13, MUTED), BorderLayout.NORTH);
        component.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        component.setForeground(INK);
        component.setBackground(Color.WHITE);
        component.setPreferredSize(new Dimension(180, 40));
        if (component instanceof JTextField) {
            component.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE), new EmptyBorder(8, 10, 8, 10)));
        }
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    static JButton button(JButton button, String text, boolean primary) {
        button.setText(text);
        button.setIcon(iconoBoton(button.getIcon(), primary));
        button.setIconTextGap(10);
        button.setPreferredSize(null);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(primary ? Color.WHITE : GREEN);
        button.setBackground(primary ? GREEN : new Color(235, 245, 239));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color background = component.getBackground();
                if (button.getModel().isPressed()) background = background.darker();
                else if (button.getModel().isRollover())
                    background = primary ? background.brighter() : new Color(220, 237, 228);
                canvas.setColor(background);
                canvas.fillRoundRect(1, 1, component.getWidth() - 2, component.getHeight() - 2, 12, 12);
                if (button.hasFocus()) {
                    canvas.setColor(primary ? new Color(167, 221, 196) : GREEN);
                    canvas.setStroke(new BasicStroke(2));
                    canvas.drawRoundRect(3, 3, component.getWidth() - 7, component.getHeight() - 7, 10, 10);
                }
                canvas.dispose();
                super.paint(graphics, component);
            }
        });
        button.setRolloverEnabled(true);
        button.setFocusPainted(true);
        button.setBorder(new EmptyBorder(12, 16, 12, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JScrollPane table(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setForeground(INK);
        table.setBackground(Color.WHITE);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionBackground(new Color(220, 239, 229));
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(235, 241, 238));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        Runnable priceColumn = () -> {
            int last = table.getColumnModel().getColumnCount() - 1;
            if (last < 0) return;
            javax.swing.table.TableColumn price = table.getColumnModel().getColumn(last);
            price.setMinWidth(140);
            price.setPreferredWidth(160);
            price.setMaxWidth(200);
            price.setResizable(false);
            DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable owner, Object value,
                        boolean selected, boolean focused, int row, int column) {
                    Component cell = super.getTableCellRendererComponent(owner, value,
                            selected, focused, row, column);
                    cell.setFont(owner.getFont());
                    setBorder(new EmptyBorder(0, 10, 0, 12));
                    return cell;
                }
            };
            priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
            price.setCellRenderer(priceRenderer);
        };
        priceColumn.run();
        table.addPropertyChangeListener("model", event -> priceColumn.run());
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.setDefaultRenderer(Object.class, renderer);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    static JPanel shell(JFrame frame, String title, String subtitle) {
        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(24, 24, 20, 24));
        JPanel heading = new JPanel(new BorderLayout(16, 0));
        heading.setOpaque(false);
        JPanel titles = column();
        titles.add(label(title, 26, INK));
        titles.add(Box.createVerticalStrut(6));
        titles.add(label(subtitle, 14, MUTED));
        heading.add(titles, BorderLayout.CENTER);
        JLabel logo = MarcaEmpresa.etiquetaLogo(94,58);
        heading.add(logo, BorderLayout.WEST);
        root.add(heading, BorderLayout.NORTH);
        frame.setContentPane(root);
        frame.getRootPane().putClientProperty("tituloVista",title);
        frame.setTitle(Config.EmpresaConfig.actual().nombre+" · "+title);
        frame.setIconImage(MarcaEmpresa.icono());
        frame.setResizable(true);
        return root;
    }

    private static void finish(JFrame frame, int width, int height) {
        frame.setMinimumSize(new Dimension(Math.min(width, 900), Math.min(height, 640)));
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
    }

    public static void menu(MenuPrincipalView view) {
        JPanel root = shell(view, "Inicio", "Todo listo para una nueva jornada.");
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        JPanel sale = card("Nueva venta", "Productos, cobros y tickets.");
        sale.add(label("Todo listo para vender.", 14, MUTED), BorderLayout.CENTER);
        JPanel saleActions = column();
        saleActions.add(button(view.btnPuntoDeVenta, "Iniciar venta →", true));
        saleActions.add(Box.createVerticalStrut(8));
        saleActions.add(button(view.btnHistorial, "Historial de ventas", false));
        sale.add(saleActions, BorderLayout.SOUTH);
        JPanel products = card("Productos", "El catálogo de tu almacén.");
        products.add(label("Consultá y actualizá precios.", 14, MUTED), BorderLayout.CENTER);
        JPanel productActions=column();
        productActions.add(button(view.btnAdministracion,"Ver productos →",false));
        productActions.add(Box.createVerticalStrut(8));
        productActions.add(button(view.btnSinCodigo,"Productos sin código",false));
        products.add(productActions,BorderLayout.SOUTH);
        JPanel cash = card("Caja", "Apertura, movimientos y cierre.");
        cash.add(label("Controlá el efectivo del turno.", 14, MUTED), BorderLayout.CENTER);
        view.btnCaja.setIcon(imagen("metodo-de-pago.png", 22, 22));
        cash.add(button(view.btnCaja, "Control de caja →", false), BorderLayout.SOUTH);
        cards.add(sale);
        cards.add(products);
        cards.add(cash);
        root.add(cards, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        JLabel pie=label(Config.EmpresaConfig.actual().nombre+" · Sistema de ventas",12,MUTED);pie.putClientProperty("pieEmpresa",true);
        footer.add(pie,BorderLayout.WEST);
        footer.add(row(button(view.btnEmpresa,"Configurar negocio",false)),BorderLayout.CENTER);
        view.btnBackup.setIcon(imagen("paquete.png", 22, 22));
        footer.add(button(view.btnBackup, "Copias de seguridad", false), BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        finish(view, 1100, 440);
    }

    public static void venta(VentaView view) {
        JPanel root = shell(view, "Ventas", "Escaneá un código y presioná Enter para agregar un producto.");
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        JPanel cart = card("Carrito", "Los productos de esta compra.");
        JPanel cartBody = new JPanel(new BorderLayout(12, 12));
        cartBody.setOpaque(false);
        cartBody.add(field("Código de barras · Enter para agregar", view.txtCodigo), BorderLayout.NORTH);
        cartBody.add(table(view.tablaProductos), BorderLayout.CENTER);
        JPanel quick = column();
        JPanel quickFields = new JPanel(new GridLayout(1, 2, 12, 0));
        quickFields.setOpaque(false);
        quickFields.add(field("Producto sin código", view.cbbNombre));
        quickFields.add(field("Precio", view.txtPrecio));
        quick.add(quickFields);
        quick.add(row(button(view.btnAgregarOtro, "Agregar al carrito", true),
                button(view.btnQuitarProducto, "Quitar seleccionado", false),
                button(view.btnSinCodigo,"Administrar sin código",false)));
        cartBody.add(quick, BorderLayout.SOUTH);
        cart.add(cartBody, BorderLayout.CENTER);
        body.add(cart, BorderLayout.CENTER);

        JPanel sidebar = new Sidebar();
        sidebar.setOpaque(false);
        JPanel payment = card("Cobro", "Revisá el total antes de cobrar.");
        JPanel amounts = column();
        amounts.add(field("Total a pagar", view.txtTotalAPagar));
        view.txtTotalAPagar.setFont(new Font("Segoe UI", Font.BOLD, 32));
        view.txtTotalAPagar.setForeground(GREEN);
        view.txtTotalAPagar.setPreferredSize(new Dimension(180, 62));
        amounts.add(Box.createVerticalStrut(14));
        amounts.add(field("Paga con", view.txtPagaCon));
        amounts.add(Box.createVerticalStrut(10));
        amounts.add(button(view.btnObtenerVuelto, "Calcular vuelto", false));
        amounts.add(Box.createVerticalStrut(14));
        amounts.add(field("Vuelto", view.txtVuelto));
        view.txtVuelto.setForeground(GREEN);
        view.txtVuelto.setFont(new Font("Segoe UI", Font.BOLD, 24));
        payment.add(amounts, BorderLayout.CENTER);
        amounts.add(field("Medio de pago", view.medioPago));
        JPanel cobro = column();
        cobro.add(button(view.btnCobrar, "Confirmar cobro", true));
        cobro.add(Box.createVerticalStrut(8));
        cobro.add(button(view.btnImprimir, "Imprimir ticket", false));
        payment.add(cobro, BorderLayout.SOUTH);
        sidebar.add(payment, BorderLayout.NORTH);
        JPanel search = card("Consultar precio", "Ingresá un código y presioná Enter.");
        JPanel searchFields = column();
        searchFields.add(field("Código", view.txtBuscarCodigo));
        searchFields.add(Box.createVerticalStrut(8));
        searchFields.add(field("Producto", view.txtNombreBuscado));
        searchFields.add(Box.createVerticalStrut(8));
        searchFields.add(field("Precio", view.txtPrecioBuscado));
        searchFields.add(row(button(view.btnLimpiarBuscar, "Limpiar", false)));
        search.add(searchFields, BorderLayout.CENTER);
        sidebar.add(search, BorderLayout.CENTER);
        JScrollPane sideScroll = new JScrollPane(sidebar);
        sideScroll.setBorder(null);
        sideScroll.setPreferredSize(new Dimension(340, 0));
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);
        body.add(sideScroll, BorderLayout.EAST);
        root.add(body, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(row(button(view.btnNuevaCompra, "Nueva compra", false),
                button(view.btnVerPrecio, "Ver precio · F4", false)), BorderLayout.NORTH);
        bottom.add(label("F1: ayuda   ·   F3: pago   ·   F5: lector   ·   F6: sin código", 13, MUTED), BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);
        // Reuse the existing shortcut action so mouse and keyboard open the same window.
        view.btnVerPrecio.addActionListener(event -> {
            javax.swing.Action action = view.btnVerPrecio.getActionMap().get("pressedF4");
            if (action != null) action.actionPerformed(event);
        });
        finish(view, 1180, 800);
    }

    public static void administracion(AdministracionView view) {
        JPanel root = shell(view, "Productos", "Mantené el catálogo y los precios actualizados.");
        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setOpaque(false);
        JPanel editor = card("Datos del producto", "Agregá o actualizá un producto.");
        editor.setPreferredSize(new Dimension(340, 0));
        JPanel fields = column();
        fields.add(field("Código de barras", view.txtCodigo));
        fields.add(Box.createVerticalStrut(14));
        fields.add(field("Nombre", view.txtNombre));
        fields.add(Box.createVerticalStrut(14));
        fields.add(field("Precio", view.txtPrecio));
        fields.add(Box.createVerticalStrut(20));
        fields.add(button(view.btnAgregar, "Agregar producto", true));
        fields.add(Box.createVerticalStrut(10));
        fields.add(button(view.btnModificar, "Guardar cambios", false));
        fields.add(Box.createVerticalStrut(10));
        fields.add(button(view.btnVaciarCampos, "Limpiar campos", false));
        fields.add(Box.createVerticalGlue());
        editor.add(fields, BorderLayout.CENTER);
        body.add(editor, BorderLayout.WEST);
        JPanel catalog = card("Catálogo", "Seleccioná un producto para editarlo o eliminarlo.");
        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setOpaque(false);
        JPanel searches = new JPanel(new GridLayout(1, 2, 12, 0));
        searches.setOpaque(false);
        searches.add(field("Buscar por nombre", view.txtBuscarPorNombre));
        searches.add(field("Código · Enter para buscar", view.txtBuscarPorCodigo));
        content.add(searches, BorderLayout.NORTH);
        content.add(table(view.tablaProductos), BorderLayout.CENTER);
        content.add(row(button(view.btnEditar, "Editar seleccionado", false),
                button(view.btnEliminar, "Eliminar", false),
                button(view.btnLimpiarBusqueda, "Limpiar búsqueda", false)), BorderLayout.SOUTH);
        view.btnEliminar.setForeground(new Color(171, 53, 53));
        catalog.add(content, BorderLayout.CENTER);
        body.add(catalog, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);
        finish(view, 1120, 700);
        root.add(row(button(view.btnSinCodigo,"Administrar productos sin código",false)),BorderLayout.SOUTH);
    }

    public static void precio(VerPrecio view) {
        JPanel root = shell(view, "Consultar precio", "Escaneá un producto o escribí su código.");
        JPanel result = card("Consulta rápida", "Presioná Enter para ver el resultado.");
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(field("Código de barras", view.txtCodigo), BorderLayout.NORTH);
        JTextArea name = new JTextArea(3, 24);
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(INK);
        name.setEditable(false);
        name.setFocusable(false);
        name.setLineWrap(true);
        name.setWrapStyleWord(true);
        name.setOpaque(false);
        name.setText(view.labelNombre.getText());
        // Keep the controller's existing label as the result source.
        view.labelNombre.addPropertyChangeListener("text", event -> {
            name.setText((String) event.getNewValue());
            name.setCaretPosition(0);
        });
        JScrollPane nameScroll = new JScrollPane(name);
        nameScroll.setBorder(null);
        nameScroll.setOpaque(false);
        nameScroll.getViewport().setOpaque(false);
        content.add(nameScroll, BorderLayout.CENTER);
        view.labelPrecio.setFont(new Font("Segoe UI", Font.BOLD, 40));
        view.labelPrecio.setForeground(GREEN);
        view.labelPrecio.setPreferredSize(new Dimension(400, 60));
        content.add(view.labelPrecio, BorderLayout.SOUTH);
        result.add(content, BorderLayout.CENTER);
        root.add(result, BorderLayout.CENTER);
        root.add(button(view.btnCerrar, "Cerrar · Esc", false), BorderLayout.SOUTH);
        view.btnCerrar.addActionListener(event -> view.dispose());
        finish(view, 640, 580);
        view.setMinimumSize(new Dimension(560, 580));
    }
}
