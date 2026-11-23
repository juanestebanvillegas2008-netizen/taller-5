import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Documento {

    private final String nombre;
    private final String apellido1;
    private final String apellido2;
    private final String tipoDocumento;
    private final String nombreCompleto; // se calcula una sola vez (más rápido al comparar)

    public Documento() {
        this("", "", "", "");
    }

    public Documento(String apellido1, String apellido2, String nombre, String tipoDocumento) {
        this.nombre = nombre;
        this.apellido1 = apellido1;
        this.apellido2 = apellido2;
        this.tipoDocumento = tipoDocumento;
        this.nombreCompleto = apellido1 + " " + apellido2 + " " + nombre;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    // ********** Atributos y métodos estáticos **********
    public static Documento[] documentos;

    private static long inicioCronometro;

    private static void iniciarCronometro() {
        inicioCronometro = System.nanoTime();
    }

    // Formato HH:MM:SS.cc (centésimas), como en el taller
    private static String obtenerTextoTiempoCronometro() {
        long ms = (System.nanoTime() - inicioCronometro) / 1_000_000;
        long h = ms / 3_600_000;
        long m = (ms / 60_000) % 60;
        long s = (ms / 1000) % 60;
        long cs = (ms % 1000) / 10;
        return String.format("%02d:%02d:%02d.%02d", h, m, s, cs);
    }

    public static void obtenerLista(String nombreArchivo) {
        List<Documento> lista = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(nombreArchivo), StandardCharsets.UTF_8)) {
            br.readLine(); // encabezado (trae el BOM, se descarta)
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] t = linea.split(";");
                if (t.length >= 4) {
                    lista.add(new Documento(t[0], t[1], t[2], t[3]));
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer el archivo: " + ex.getMessage());
        }
        documentos = lista.toArray(new Documento[0]);
    }

    private static void intercambiar(int origen, int destino) {
        Documento t = documentos[origen];
        documentos[origen] = documentos[destino];
        documentos[destino] = t;
    }

    // criterio 0: Nombre y luego Documento | criterio 1: Documento y luego Nombre
    private static boolean esMayor(Documento dI, Documento dJ, int criterio) {
        int porNombre = dI.getNombreCompleto().compareTo(dJ.getNombreCompleto());
        int porTipo = dI.getTipoDocumento().compareTo(dJ.getTipoDocumento());
        if (criterio == 0) {
            return porNombre > 0 || (porNombre == 0 && porTipo > 0);
        } else {
            return porTipo > 0 || (porTipo == 0 && porNombre > 0);
        }
    }

    // ********** BURBUJA (no recursivo) **********
    public static String ordenarBurbuja(int criterio) {
        iniciarCronometro();
        for (int i = 0; i < documentos.length - 1; i++) {
            for (int j = i + 1; j < documentos.length; j++) {
                if (esMayor(documentos[i], documentos[j], criterio)) {
                    intercambiar(i, j);
                }
            }
        }
        return obtenerTextoTiempoCronometro();
    }

    // ********** RÁPIDO (recursivo) **********
    public static String ordenarRapido(int criterio) {
        iniciarCronometro();
        _ordenarRapido(0, documentos.length - 1, criterio);
        return obtenerTextoTiempoCronometro();
    }

    private static void _ordenarRapido(int inicio, int fin, int criterio) {
        if (inicio >= fin) {
            return;
        }
        int posicionPivote = ubicarPivote(inicio, fin, criterio);
        _ordenarRapido(inicio, posicionPivote - 1, criterio);
        _ordenarRapido(posicionPivote + 1, fin, criterio);
    }

    private static int ubicarPivote(int inicio, int fin, int criterio) {
        // Se toma el elemento del medio como pivote (evita StackOverflow con listas ya ordenadas)
        intercambiar(inicio, (inicio + fin) >>> 1);
        Documento dPivote = documentos[inicio];
        int posicionPivote = inicio;
        for (int i = inicio + 1; i <= fin; i++) {
            if (esMayor(dPivote, documentos[i], criterio)) {
                posicionPivote++;
                if (i != posicionPivote) {
                    intercambiar(i, posicionPivote);
                }
            }
        }
        if (inicio != posicionPivote) {
            intercambiar(inicio, posicionPivote);
        }
        return posicionPivote;
    }

    // ********** INSERCIÓN (no recursivo) **********
    // Toma cada elemento y lo "inserta" en su lugar dentro de la parte ya ordenada.
    public static String ordenarInsercion(int criterio) {
        iniciarCronometro();
        for (int i = 1; i < documentos.length; i++) {
            Documento clave = documentos[i];
            int j = i - 1;
            while (j >= 0 && esMayor(documentos[j], clave, criterio)) {
                documentos[j + 1] = documentos[j]; // desplaza a la derecha
                j--;
            }
            documentos[j + 1] = clave;
        }
        return obtenerTextoTiempoCronometro();
    }

    // ********** MEZCLA / MERGESORT (recursivo) **********
    // Divide la lista por la mitad, ordena cada mitad y las mezcla.
    public static String ordenarMezcla(int criterio) {
        iniciarCronometro();
        Documento[] auxiliar = new Documento[documentos.length];
        _ordenarMezcla(0, documentos.length - 1, criterio, auxiliar);
        return obtenerTextoTiempoCronometro();
    }

    private static void _ordenarMezcla(int inicio, int fin, int criterio, Documento[] aux) {
        if (inicio >= fin) { // caso base: 0 o 1 elemento
            return;
        }
        int medio = (inicio + fin) >>> 1;
        _ordenarMezcla(inicio, medio, criterio, aux);
        _ordenarMezcla(medio + 1, fin, criterio, aux);
        mezclar(inicio, medio, fin, criterio, aux);
    }

    private static void mezclar(int inicio, int medio, int fin, int criterio, Documento[] aux) {
        for (int k = inicio; k <= fin; k++) {
            aux[k] = documentos[k];
        }
        int i = inicio;     // recorre la mitad izquierda
        int j = medio + 1;  // recorre la mitad derecha
        for (int k = inicio; k <= fin; k++) {
            if (i > medio) {
                documentos[k] = aux[j++];
            } else if (j > fin) {
                documentos[k] = aux[i++];
            } else if (esMayor(aux[i], aux[j], criterio)) {
                documentos[k] = aux[j++];
            } else {
                documentos[k] = aux[i++];
            }
        }
    }
}