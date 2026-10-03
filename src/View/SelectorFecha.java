package View;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import javax.swing.*;

/** Popup calendar shared by history and charts; dates are picked, never typed. */
final class SelectorFecha extends JDateChooser {
    SelectorFecha(LocalDate fecha) {
        setLocale(new Locale("es","AR"));
        setDateFormatString("dd MMM yyyy");
        setPreferredSize(new Dimension(165,34));
        getJCalendar().getYearChooser().setStartYear(1);
        getJCalendar().getYearChooser().setEndYear(9999);
        getJCalendar().setTodayButtonVisible(true);
        getJCalendar().setNullDateButtonVisible(false);
        JTextFieldDateEditor editor=(JTextFieldDateEditor)getDateEditor();
        editor.setEditable(false);
        editor.setToolTipText("Elegí una fecha en el calendario");
        editor.getAccessibleContext().setAccessibleName("Fecha seleccionada");
        getCalendarButton().setToolTipText("Abrir calendario");
        getCalendarButton().getAccessibleContext().setAccessibleName("Abrir calendario");
        editor.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { abrirCalendario(); }
        });
        String action="abrirCalendario";
        editor.getInputMap().put(KeyStroke.getKeyStroke("alt DOWN"),action);
        editor.getInputMap().put(KeyStroke.getKeyStroke("SPACE"),action);
        editor.getActionMap().put(action,new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { abrirCalendario(); }
        });
        setFecha(fecha);
    }
    private void abrirCalendario() { if(isEnabled()) getCalendarButton().doClick(); }
    LocalDate getFecha() {
        Date fecha=getDate();
        if(fecha==null) throw new IllegalArgumentException("Seleccioná una fecha en el calendario.");
        return java.time.Instant.ofEpochMilli(fecha.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
    void setFecha(LocalDate fecha) {
        setDate(Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}
