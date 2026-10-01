import View.VerPrecio;
import java.awt.*;
import javax.swing.*;

public final class ConsultaPrecioCheck {
    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents())
            if (child instanceof Container) layout((Container) child);
    }
    private static void check(VerPrecio view, Dimension size) {
        Container content = view.getContentPane();
        content.setSize(size.width - 16, size.height - 39);
        view.labelNombre.setText("Yerba mate elaborada con palo selección especial paquete de un kilogramo");
        view.labelPrecio.setText("$ 1234567890");
        layout(content);
        JLabel price = view.labelPrecio;
        if (price.getHeight() < price.getFontMetrics(price.getFont()).getHeight())
            throw new AssertionError("Price is vertically clipped");
        if (price.getWidth() < price.getFontMetrics(price.getFont()).stringWidth(price.getText()))
            throw new AssertionError("Price is horizontally clipped");
        Rectangle bounds = SwingUtilities.convertRectangle(price.getParent(), price.getBounds(), content);
        if (!new Rectangle(0, 0, content.getWidth(), content.getHeight()).contains(bounds))
            throw new AssertionError("Price is outside the window");
        view.labelNombre.setText("");
        view.labelPrecio.setText("");
        layout(content);
        view.labelNombre.setText("Pan");
        view.labelPrecio.setText("$ 500");
        layout(content);
        if (price.getHeight() < 60) throw new AssertionError("Result space collapses between queries");
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            VerPrecio view = new VerPrecio();
            try {
                check(view, view.getSize());
                check(view, view.getMinimumSize());
                System.out.println("OK: price visible with long names, large amounts and repeated queries");
            } finally { view.dispose(); }
        });
    }
}
