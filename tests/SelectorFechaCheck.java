package View;

import java.time.LocalDate;
import com.toedter.calendar.JTextFieldDateEditor;
import javax.swing.*;

public final class SelectorFechaCheck {
    private static void check(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SelectorFecha selector=new SelectorFecha(LocalDate.of(2024,2,29));
            JTextFieldDateEditor editor=(JTextFieldDateEditor)selector.getDateEditor();
            check(!editor.isEditable(),"Dates picked from calendar without typing");
            for(LocalDate fecha:new LocalDate[]{LocalDate.of(1850,1,1),LocalDate.of(2100,12,31),LocalDate.of(2024,2,29),LocalDate.now()}) {
                selector.setFecha(fecha);check(selector.getFecha().equals(fecha),"Date and timezone roundtrip: "+fecha);
            }
            check(selector.getJCalendar().getYearChooser().getStartYear()==1 && selector.getJCalendar().getYearChooser().getEndYear()==9999,"Year selection no longer limited to old combo range");
            check(selector.getJCalendar().getLocale().getLanguage().equals("es"),"Calendar localized in Spanish");
            check(selector.getJCalendar().isTodayButtonVisible(),"Today shortcut in calendar");
            selector.setEnabled(false);check(!editor.isEnabled() && !selector.getCalendarButton().isEnabled(),"Calendar disabled while loading");
            selector.setEnabled(true);check(editor.isEnabled() && selector.getCalendarButton().isEnabled() && !editor.isEditable(),"Reenabled without manual date input");
            selector.setDate(null);
            try {selector.getFecha();throw new AssertionError("Null date must be rejected");}catch(IllegalArgumentException expected){}
            selector.cleanup();
            System.out.println("SelectorFechaCheck OK: calendar, Spanish, wide year range, dates and loading state");
        });
    }
}
