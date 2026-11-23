import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

public class FrmOrdenamiento extends JFrame {

    private final String archivo;
    private final JComboBox<String> cmbCriterio =
            new JComboBox<>(new String[]{"Nombre - Documento", "Documento - Nombre"});
    private final JTextField txtTiempo = new JTextField("00:00:00.00", 9);
    private final JButton btnBurbuja = new JButton("Burbuja");
    private final JButton btnRapido = new JButton("Rápido");
    private final JButton btnInsercion = new JButton("Inserción");
    private final JButton btnMezcla = new JButton("Mezcla");
    private final JButton btnRecargar = new JButton("Recargar datos");

    private final AbstractTableModel modelo = new AbstractTableModel() {
        private final String[] columnas = {"Nombre", "Tipo Documento"};

        public int getRowCount() {
            return Documento.documentos == null ? 0 : Documento.documentos.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int c) {
            return columnas[c];
        }

        public Object getValueAt(int f, int c) {
            Documento d = Documento.documentos[f];
            return c == 0 ? d.getNombreCompleto() : d.getTipoDocumento();
        }
    };

    public FrmOrdenamiento(String archivo) {
        super("Ordenamiento de documentos");
        this.archivo = archivo;

        JPanel barra = new JPanel();
        barra.add(btnBurbuja);
        barra.add(btnRapido);
        barra.add(btnInsercion);
        barra.add(btnMezcla);
        barra.add(cmbCriterio);
        barra.add(new JLabel("Tiempo:"));
        txtTiempo.setEditable(false);
        barra.add(txtTiempo);
        barra.add(btnRecargar);

        add(barra, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);

        btnBurbuja.addActionListener(e -> ordenar("burbuja"));
        btnRapido.addActionListener(e -> ordenar("rapido"));
        btnInsercion.addActionListener(e -> ordenar("insercion"));
        btnMezcla.addActionListener(e -> ordenar("mezcla"));
        btnRecargar.addActionListener(e -> cargar());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        cargar();
    }

    private void cargar() {
        Documento.obtenerLista(archivo);
        txtTiempo.setText("00:00:00.00");
        modelo.fireTableDataChanged();
    }

    private void habilitar(boolean b) {
        for (JButton btn : new JButton[]{btnBurbuja, btnRapido, btnInsercion, btnMezcla, btnRecargar}) {
            btn.setEnabled(b);
        }
        cmbCriterio.setEnabled(b);
    }

    // El ordenamiento corre en otro hilo para que la ventana no se congele
    private void ordenar(String algoritmo) {
        int criterio = cmbCriterio.getSelectedIndex();
        if (criterio < 0 || Documento.documentos == null) {
            return;
        }
        habilitar(false);
        txtTiempo.setText("ordenando...");
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                switch (algoritmo) {
                    case "burbuja":   return Documento.ordenarBurbuja(criterio);
                    case "rapido":    return Documento.ordenarRapido(criterio);
                    case "insercion": return Documento.ordenarInsercion(criterio);
                    default:          return Documento.ordenarMezcla(criterio);
                }
            }

            @Override
            protected void done() {
                try {
                    txtTiempo.setText(get());
                } catch (Exception ex) {
                    txtTiempo.setText("error");
                }
                modelo.fireTableDataChanged();
                habilitar(true);
            }
        }.execute();
    }

    public static void main(String[] args) {
        String archivo = args.length > 0 ? args[0] : "Datos.csv";
        SwingUtilities.invokeLater(() -> new FrmOrdenamiento(archivo).setVisible(true));
    }
}