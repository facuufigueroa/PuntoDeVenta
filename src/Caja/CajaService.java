package Caja;

import DataBase.ConexionBD;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import Model.Compra;
import java.time.LocalDate;

/** Persistent cash sessions. All writes serialize through a single database row. */
public final class CajaService {
    interface Connections { Connection open() throws SQLException; }
    private final Connections connections;
    public CajaService() { this(ConexionBD::getConnection); }
    CajaService(Connections connections) { this.connections = connections; }
    public static final String[] MEDIOS = {"Efectivo", "Tarjeta", "Transferencia"};
    public static final class Estado {
        public final long id;
        public final Timestamp apertura;
        Estado(long id,Timestamp apertura) { this.id=id;this.apertura=apertura; }
        public boolean pendiente(LocalDate hoy) { return id!=0 && apertura!=null && apertura.toLocalDateTime().toLocalDate().isBefore(hoy); }
    }
    public Estado estado() throws SQLException {
        try(Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT id,apertura FROM caja_sesion WHERE cierre IS NULL")) {
            return r.next() ? new Estado(r.getLong(1),r.getTimestamp(2)) : new Estado(0,null);
        }
    }
    public static BigDecimal importe(String text) {
        try {
            BigDecimal value = new BigDecimal(text.trim().replace(',', '.')).setScale(2, java.math.RoundingMode.UNNECESSARY);
            if (value.signum() < 0 || value.precision() > 14) throw new IllegalArgumentException();
            return value;
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Ingresá un importe válido, sin separadores de miles y con hasta dos decimales.");
        }
    }
    private Connection connect() throws SQLException {
        Connection c = connections.open();
        if (c == null) throw new SQLException("No se pudo conectar a la base de datos.");
        return c;
    }
    private void lock(Connection c) throws SQLException {
        // UPDATE takes the InnoDB row lock until commit, including when the value is unchanged.
        try (Statement s = c.createStatement()) { s.executeUpdate("UPDATE caja_control SET id=id WHERE id=1"); }
        try (PreparedStatement p = c.prepareStatement("SELECT id FROM caja_control WHERE id=1"); ResultSet r = p.executeQuery()) {
            if (!r.next()) throw new SQLException("Importá database/caja.sql para habilitar caja.");
        }
    }
    private long active(Connection c) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id FROM caja_sesion WHERE cierre IS NULL")) {
            return r.next() ? r.getLong(1) : 0;
        }
    }
    public void abrir(BigDecimal fondo) throws SQLException {
        importe(fondo.toPlainString());
        transact(c -> {
            if (active(c) != 0) throw new SQLException("Ya hay una caja abierta.");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO caja_sesion (fondo) VALUES (?)")) { p.setBigDecimal(1, fondo); p.executeUpdate(); }
        });
    }
    public void movimiento(String tipo, String medio, BigDecimal monto, String detalle) throws SQLException {
        if (!Arrays.asList("VENTA", "INGRESO", "RETIRO").contains(tipo) || !Arrays.asList(MEDIOS).contains(medio)) throw new IllegalArgumentException("Movimiento inválido.");
        importe(monto.toPlainString());
        if (monto.signum() <= 0 || detalle == null || detalle.trim().isEmpty() || detalle.length() > 4000) throw new IllegalArgumentException("Indicá un monto positivo y un detalle de hasta 4000 caracteres.");
        transact(c -> {
            long id = active(c);
            if (id == 0) throw new SQLException("Primero abrí una caja desde el menú principal.");
            if (tipo.equals("RETIRO") && saldo(c, id).compareTo(monto) < 0) throw new SQLException("El retiro supera el efectivo disponible.");
            try (PreparedStatement p = c.prepareStatement("INSERT INTO caja_movimiento (sesion,tipo,medio,monto,detalle) VALUES (?,?,?,?,?)")) {
                p.setLong(1,id); p.setString(2,tipo); p.setString(3,medio); p.setBigDecimal(4,monto); p.setString(5,detalle); p.executeUpdate();
            }
        });
    }
    private BigDecimal saldo(Connection c, long id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT fondo + COALESCE((SELECT SUM(CASE WHEN tipo IN ('RETIRO','REVERSO') THEN -monto ELSE monto END) FROM caja_movimiento WHERE sesion=? AND medio='Efectivo'),0) FROM caja_sesion WHERE id=?")) {
            p.setLong(1,id); p.setLong(2,id);
            try (ResultSet r = p.executeQuery()) { r.next(); return r.getBigDecimal(1); }
        }
    }
    public void cerrar(long esperada, BigDecimal contado) throws SQLException {
        importe(contado.toPlainString());
        transact(c -> {
            long id = active(c);
            if (id == 0 || id != esperada) throw new SQLException("La caja cambió. Actualizá la pantalla.");
            try (PreparedStatement p = c.prepareStatement("UPDATE caja_sesion SET cierre=CURRENT_TIMESTAMP, contado=?, esperado=? WHERE id=?")) {
                p.setBigDecimal(1,contado); p.setBigDecimal(2,saldo(c,id)); p.setLong(3,id); p.executeUpdate();
            }
        });
    }
    private interface Work { void run(Connection c) throws SQLException; }
    private void transact(Work work) throws SQLException {
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try { lock(c); work.run(c); c.commit(); }
            catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public static final class Resumen {
        public long id;
        public BigDecimal efectivo = BigDecimal.ZERO;
        public final Map<String,BigDecimal> ventas = new LinkedHashMap<>();
        public final List<Object[]> movimientos = new ArrayList<>(), sesiones = new ArrayList<>();
    }
    public Resumen resumen() throws SQLException {
        Resumen out = new Resumen();
        try (Connection c = connect()) {
            c.setAutoCommit(false);
            try {
                lock(c); out.id = active(c);
                if (out.id != 0) out.efectivo = saldo(c,out.id);
                for (String medio : MEDIOS) out.ventas.put(medio,BigDecimal.ZERO);
                try (PreparedStatement p = c.prepareStatement("SELECT medio,SUM(CASE WHEN tipo='REVERSO' THEN -monto ELSE monto END) FROM caja_movimiento WHERE sesion=? AND tipo IN ('VENTA','REVERSO') GROUP BY medio")) {
                    p.setLong(1,out.id);
                    try (ResultSet r = p.executeQuery()) { while(r.next()) out.ventas.put(r.getString(1),r.getBigDecimal(2)); }
                }
                try (PreparedStatement p = c.prepareStatement("SELECT fecha,tipo,medio,monto,detalle FROM caja_movimiento WHERE sesion=? ORDER BY id DESC")) {
                    p.setLong(1,out.id);
                    try (ResultSet r = p.executeQuery()) { while (r.next()) out.movimientos.add(new Object[]{r.getTimestamp(1),r.getString(2),r.getString(3),r.getBigDecimal(4),r.getString(5)}); }
                }
                try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT id,apertura,cierre,fondo,esperado,contado,contado-esperado FROM caja_sesion ORDER BY id DESC LIMIT 100")) {
                    while (r.next()) out.sesiones.add(new Object[]{r.getLong(1),r.getTimestamp(2),r.getTimestamp(3),r.getBigDecimal(4),r.getBigDecimal(5),r.getBigDecimal(6),r.getBigDecimal(7)});
                }
                c.commit(); return out;
            } catch (SQLException e) { c.rollback(); throw e; }
        }
    }

    /** Header, product snapshot and drawer movement commit together. Token allows safe retries. */
    public void cobrar(String solicitud, String medio, List<Compra> items) throws SQLException {
        if (solicitud==null || !solicitud.matches("[a-fA-F0-9-]{36}") || !Arrays.asList(MEDIOS).contains(medio) || items==null || items.isEmpty())
            throw new IllegalArgumentException("Datos de venta inválidos.");
        BigDecimal total = BigDecimal.ZERO;
        List<String> nombres = new ArrayList<>(); List<BigDecimal> precios = new ArrayList<>();
        StringBuilder detalle = new StringBuilder();
        for (Compra item : items) {
            String nombre = item.getNombre();
            if (nombre==null || nombre.trim().isEmpty() || nombre.length()>255) throw new IllegalArgumentException("El nombre del producto debe tener entre 1 y 255 caracteres.");
            BigDecimal precio = importe(item.getPrecio().replace("$",""));
            nombres.add(nombre); precios.add(precio); total=total.add(precio);
            detalle.append(nombre).append(" · $").append(precio).append('\n');
        }
        final BigDecimal monto = importe(total.toPlainString());
        final String descripcion = detalle.toString();
        if(monto.signum()<=0 || descripcion.length()>4000) throw new IllegalArgumentException("La venta debe tener un total positivo y un detalle de hasta 4000 caracteres.");
        transact(c -> {
            try (PreparedStatement p = c.prepareStatement("SELECT m.monto,m.medio,m.detalle FROM venta v JOIN caja_movimiento m ON m.id=v.movimiento WHERE v.solicitud=?")) {
                p.setString(1,solicitud);
                try (ResultSet r = p.executeQuery()) {
                    if(r.next()) {
                        if(r.getBigDecimal(1).compareTo(monto)!=0 || !medio.equals(r.getString(2)) || !descripcion.equals(r.getString(3))) throw new SQLException("La solicitud ya corresponde a otra venta.");
                        return;
                    }
                }
            }
            long sesion = active(c);
            if(sesion==0) throw new SQLException("Primero abrí una caja desde el menú principal.");
            long movimiento = insertarMovimiento(c,sesion,"VENTA",medio,monto,descripcion);
            long venta;
            try (PreparedStatement p = c.prepareStatement("INSERT INTO venta (movimiento,solicitud) VALUES (?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setLong(1,movimiento); p.setString(2,solicitud); p.executeUpdate();
                try(ResultSet r = p.getGeneratedKeys()) { if(!r.next()) throw new SQLException("No se obtuvo el identificador de venta."); venta=r.getLong(1); }
            }
            try(PreparedStatement p = c.prepareStatement("INSERT INTO venta_item (venta,nombre,precio) VALUES (?,?,?)")) {
                for(int i=0;i<nombres.size();i++) { p.setLong(1,venta); p.setString(2,nombres.get(i)); p.setBigDecimal(3,precios.get(i)); p.addBatch(); }
                p.executeBatch();
            }
        });
    }
    private long insertarMovimiento(Connection c,long sesion,String tipo,String medio,BigDecimal monto,String detalle) throws SQLException {
        try(PreparedStatement p = c.prepareStatement("INSERT INTO caja_movimiento (sesion,tipo,medio,monto,detalle) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1,sesion); p.setString(2,tipo); p.setString(3,medio); p.setBigDecimal(4,monto); p.setString(5,detalle); p.executeUpdate();
            try(ResultSet r = p.getGeneratedKeys()) { if(!r.next()) throw new SQLException("No se obtuvo el identificador de movimiento."); return r.getLong(1); }
        }
    }
    public void anular(long movimiento,String motivo) throws SQLException {
        if(motivo==null || motivo.trim().isEmpty() || motivo.trim().length()>1000) throw new IllegalArgumentException("Indicá un motivo de entre 1 y 1000 caracteres.");
        transact(c -> {
            BigDecimal monto; String medio;
            try(PreparedStatement p = c.prepareStatement("SELECT m.monto,m.medio,a.movimiento FROM caja_movimiento m LEFT JOIN venta_anulacion a ON a.movimiento=m.id WHERE m.id=? AND m.tipo='VENTA'")) {
                p.setLong(1,movimiento);
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next()) throw new SQLException("No existe esa venta.");
                    if(r.getObject(3)!=null) throw new SQLException("La venta ya está anulada. Actualizá el historial.");
                    monto=r.getBigDecimal(1); medio=r.getString(2);
                }
            }
            long sesion=active(c);
            if(sesion==0) throw new SQLException("Abrí una caja para registrar la devolución.");
            if(medio.equals("Efectivo") && saldo(c,sesion).compareTo(monto)<0) throw new SQLException("No hay efectivo suficiente para devolver esta venta.");
            long ajuste=insertarMovimiento(c,sesion,"REVERSO",medio,monto,"Anulación venta #"+movimiento+": "+motivo.trim());
            try(PreparedStatement p=c.prepareStatement("INSERT INTO venta_anulacion (movimiento,ajuste,motivo) VALUES (?,?,?)")) {
                p.setLong(1,movimiento); p.setLong(2,ajuste); p.setString(3,motivo.trim()); p.executeUpdate();
            }
        });
    }
    public static final class VentaRegistro {
        public long id, caja;
        public Timestamp fecha, anulada;
        public String medio, detalle, motivo;
        public BigDecimal total;
        public final ArrayList<Compra> items = new ArrayList<>();
    }
    public static final class Historial {
        public final List<VentaRegistro> ventas = new ArrayList<>();
        public BigDecimal bruto=BigDecimal.ZERO, anulado=BigDecimal.ZERO;
    }
    public static final class Estadisticas {
        public int confirmadas, anuladas;
        public BigDecimal total=BigDecimal.ZERO, importeAnulado=BigDecimal.ZERO;
        public final Map<String,BigDecimal> medios=new LinkedHashMap<>(), productos=new LinkedHashMap<>();
    }
    public Estadisticas estadisticas(LocalDate desde,LocalDate hasta) throws SQLException {
        if(desde==null || hasta==null || hasta.isBefore(desde)) throw new IllegalArgumentException("Revisá el rango de fechas.");
        Map<Long,VentaRegistro> registros=new LinkedHashMap<>();
        // One snapshot query: no query per ticket and no mix of pre/post-cancellation data.
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT m.id,m.sesion,m.fecha,m.medio,m.monto,m.detalle,a.fecha,a.motivo,i.nombre,i.precio FROM caja_movimiento m LEFT JOIN venta_anulacion a ON a.movimiento=m.id LEFT JOIN venta v ON v.movimiento=m.id LEFT JOIN venta_item i ON i.venta=v.id WHERE m.tipo='VENTA' AND m.fecha>=? AND m.fecha<? ORDER BY m.id,i.id")) {
            p.setTimestamp(1,Timestamp.valueOf(desde.atStartOfDay())); p.setTimestamp(2,Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    long id=r.getLong(1); VentaRegistro v=registros.get(id);
                    if(v==null) { v=registro(r); registros.put(id,v); }
                    if(r.getString(9)!=null) v.items.add(new Compra(r.getString(9),"$"+r.getBigDecimal(10).toPlainString()));
                }
            }
        }
        Estadisticas out=new Estadisticas();
        for(String medio:MEDIOS) out.medios.put(medio,BigDecimal.ZERO);
        for(VentaRegistro v:registros.values()) {
            if(v.anulada!=null) { out.anuladas++; out.importeAnulado=out.importeAnulado.add(v.total); continue; }
            out.confirmadas++; out.total=out.total.add(v.total); out.medios.merge(v.medio,v.total,BigDecimal::add);
            if(v.items.isEmpty()) v.items.addAll(recuperarItems(v.detalle,v.total));
            if(v.items.isEmpty()) out.productos.merge("Sin detalle de productos",v.total,BigDecimal::add);
            else for(Compra item:v.items) out.productos.merge(item.getNombre(),importe(item.getPrecio().replace("$","")),BigDecimal::add);
        }
        return out;
    }
    public Historial historial(LocalDate desde,LocalDate hasta,String medio) throws SQLException {
        if(desde==null || hasta==null || hasta.isBefore(desde) || (medio!=null && !Arrays.asList(MEDIOS).contains(medio))) throw new IllegalArgumentException("Revisá el rango de fechas y el medio de pago.");
        Historial out=new Historial();
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement("SELECT m.id,m.sesion,m.fecha,m.medio,m.monto,m.detalle,a.fecha,a.motivo FROM caja_movimiento m LEFT JOIN venta_anulacion a ON a.movimiento=m.id WHERE m.tipo='VENTA' AND m.fecha>=? AND m.fecha<?"+(medio==null ? "" : " AND m.medio=?")+" ORDER BY m.id DESC")) {
            p.setTimestamp(1,Timestamp.valueOf(desde.atStartOfDay())); p.setTimestamp(2,Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            if(medio!=null) p.setString(3,medio);
            try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    VentaRegistro v=registro(r); out.ventas.add(v); out.bruto=out.bruto.add(v.total);
                    if(v.anulada!=null) out.anulado=out.anulado.add(v.total);
                }
            }
        }
        return out;
    }
    private VentaRegistro registro(ResultSet r) throws SQLException {
        VentaRegistro v=new VentaRegistro(); v.id=r.getLong(1); v.caja=r.getLong(2); v.fecha=r.getTimestamp(3); v.medio=r.getString(4); v.total=r.getBigDecimal(5); v.detalle=r.getString(6); v.anulada=r.getTimestamp(7); v.motivo=r.getString(8); return v;
    }
    /** Older cash receipts already stored one product and price per line. */
    static ArrayList<Compra> recuperarItems(String detalle, BigDecimal total) {
        ArrayList<Compra> items=new ArrayList<>();
        if(detalle==null || total==null) return items;
        BigDecimal suma=BigDecimal.ZERO;
        for(String linea:detalle.split("\\r?\\n",-1)) {
            if(linea.trim().isEmpty()) continue;
            int separador=linea.lastIndexOf(" · $");
            if(separador<=0) return new ArrayList<>();
            String nombre=linea.substring(0,separador).trim();
            String precio=linea.substring(separador+4).trim();
            if(nombre.isEmpty() || !precio.matches("[0-9]+([.,][0-9]{1,2})?")) return new ArrayList<>();
            try {
                BigDecimal importe=importe(precio);
                suma=suma.add(importe); items.add(new Compra(nombre,"$"+importe.toPlainString()));
            } catch(IllegalArgumentException e) { return new ArrayList<>(); }
        }
        // Never print a partial reconstruction or a ticket with a different total.
        return suma.compareTo(total)==0 ? items : new ArrayList<>();
    }
    public VentaRegistro detalleVenta(long id) throws SQLException {
        try(Connection c=connect()) {
            c.setAutoCommit(false);
            try {
                lock(c); VentaRegistro v;
                try(PreparedStatement p=c.prepareStatement("SELECT m.id,m.sesion,m.fecha,m.medio,m.monto,m.detalle,a.fecha,a.motivo FROM caja_movimiento m LEFT JOIN venta_anulacion a ON a.movimiento=m.id WHERE m.id=? AND m.tipo='VENTA'")) {
                    p.setLong(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Venta no encontrada."); v=registro(r); }
                }
                try(PreparedStatement p=c.prepareStatement("SELECT i.nombre,i.precio FROM venta_item i JOIN venta v ON v.id=i.venta WHERE v.movimiento=? ORDER BY i.id")) {
                    p.setLong(1,id); try(ResultSet r=p.executeQuery()) { while(r.next()) v.items.add(new Compra(r.getString(1),"$"+r.getBigDecimal(2).toPlainString())); }
                }
                if(v.items.isEmpty()) v.items.addAll(recuperarItems(v.detalle,v.total));
                c.commit(); return v;
            } catch(SQLException e) { c.rollback(); throw e; }
        }
    }
}
