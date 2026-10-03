
package Reporte;


import Model.Compra;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import net.sf.jasperreports.engine.util.JRLoader;
import static net.sf.jasperreports.engine.util.JRLoader.getResource;
import net.sf.jasperreports.view.JasperViewer;



public class Reporte {
    
 
    
    public void conexionReporte(String total,ArrayList<Compra> lista){
        conexionReporte(total,lista,new java.util.Date());
    }

    public void conexionReporte(String total,ArrayList<Compra> lista,java.util.Date fecha){
       
        
        try {
            
            HashMap parametro = new HashMap();
            parametro.putAll(Config.EmpresaConfig.actual().parametros());
            
            parametro.put("total", total);
            parametro.put("fecha", fecha);
           
    
            
            JasperReport jasperMasterReport;
            jasperMasterReport = (JasperReport) JRLoader.loadObject(getClass().getResource("/Reporte/ticket.jasper"));
          
          
            JasperPrint mostrarReporte = JasperFillManager.fillReport(jasperMasterReport,parametro, new JRBeanCollectionDataSource(lista));
            JasperViewer reporteMaster = new JasperViewer(mostrarReporte,false);
            reporteMaster.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            reporteMaster.setVisible(true);
           
            
        } catch (JRException ex) {
            JOptionPane.showMessageDialog(null, ">>" + ex);
        }
    }
    
    
}
