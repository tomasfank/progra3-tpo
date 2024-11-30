package org.example.implementation;

import org.example.classes.Cultivo;
import org.example.classes.CultivoSeleccionado;
import org.example.classes.Coordenada;
import org.example.interfaces.PlanificarCultivos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {

    // Cálculo de complejidad temporal teórica desarrollado
    // Ejemplos para el cálculo de la complejidad práctica
    // Dimensión debe ser dinámica
    // 3 ciclos consecutivos
    // La elección del cultivo que se repite debe ser basada en cual conviene más… debe evaluarse al final


    // Variable para guardar la mejor ganancia.
    private double mejorGanancia = Double.NEGATIVE_INFINITY; // Double.NEGATIVE_INFINITY representa el valor negativo más pequeño posible.
    // Lista para almacenar objetos del tipo "CultivoSeleccionado"
    private List<CultivoSeleccionado> mejorConfiguracion = new ArrayList<>();
    // Tamaño del campo
    private static final int CAMPO_SIZE = 100;
    // Variable para guardar el cultivo que podrá ser plantado multiples veces
    private String cultivoMultiple;



    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        // Creamos una matriz booleana para almacenar las parcelas que iremos utilizando.
        boolean[][] parcelasUsadas = new boolean[CAMPO_SIZE][CAMPO_SIZE];
        mejorGanancia = Double.NEGATIVE_INFINITY;
        mejorConfiguracion.clear();

        // Filtrar cultivos por temporada
        List<Cultivo> cultivosTemporada = new ArrayList<>();
        for (Cultivo cultivo : cultivosDisponibles) {
            if (cultivo.getTemporadaOptima().equals(temporada)) {
                cultivosTemporada.add(cultivo);
            }
        }

        double gananciaMaximaPorParcela = calcularGananciaMaximaPorParcela(cultivosTemporada);

        Map<String, Integer> cultivosUsados = new HashMap<>();

        backtracking(cultivosTemporada, new ArrayList<>(), 0, 0, parcelasUsadas, riesgos,
                gananciaMaximaPorParcela, cultivosUsados, cultivoMultiple);

        return mejorConfiguracion;
    }

    private void backtracking(List<Cultivo> cultivos, List<CultivoSeleccionado> configuracionActual,
                              int indiceCultivo, double gananciaActual, boolean[][] parcelasUsadas,
                              double[][] riesgos, double gananciaMaximaPorParcela,
                              Map<String, Integer> cultivosUsados, String cultivoMultiple) {

        int parcelasLibres = contarParcelasLibres(parcelasUsadas);
        double gananciaEstimadaRestante = parcelasLibres * gananciaMaximaPorParcela;

        if (gananciaActual + gananciaEstimadaRestante <= mejorGanancia) {
            return; // Podar esta rama
        }

        if (indiceCultivo >= cultivos.size()) {
            if (gananciaActual > mejorGanancia) {
                mejorGanancia = gananciaActual;
                mejorConfiguracion = new ArrayList<>(configuracionActual);
            }
            return;
        }

        Cultivo cultivoActual = cultivos.get(indiceCultivo);
        String nombreCultivo = cultivoActual.getNombre();

        // Verificar si el cultivo es el único que puede ser plantado múltiples veces
        if (!nombreCultivo.equals(cultivoMultiple) && cultivosUsados.getOrDefault(nombreCultivo, 0) > 0) {
            backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                    gananciaActual, parcelasUsadas, riesgos, gananciaMaximaPorParcela, cultivosUsados, cultivoMultiple);
            return;
        }

        // Iterar sobre posibles dimensiones (N y M) cumpliendo la restricción N + M <= 11
        for (int N = 1; N <= CAMPO_SIZE; N++) {
            for (int M = 1; M <= CAMPO_SIZE; M++) {
                if (N + M <= 11) {  // Asegurarse que la suma de N + M sea <= 11
                    for (int i = 0; i < CAMPO_SIZE - N + 1; i++) {
                        for (int j = 0; j < CAMPO_SIZE - M + 1; j++) {
                            if (esValida(i, j, N, M, parcelasUsadas)) {
                                double ganancia = calcularGanancia(i, j, N, M, cultivoActual, riesgos);
                                double riesgo = calcularRiesgo(i, j, N, M, riesgos);

                                CultivoSeleccionado seleccionado = new CultivoSeleccionado();
                                seleccionado.setNombreCultivo(nombreCultivo);
                                seleccionado.setEsquinaSuperiorIzquierda(new Coordenada(i, j));
                                seleccionado.setEsquinaInferiorDerecha(new Coordenada(i + N - 1, j + M - 1));
                                seleccionado.setMontoInvertido(
                                        cultivoActual.getInversionRequerida() +
                                                (cultivoActual.getCostoPorParcela() * N * M)
                                );
                                seleccionado.setRiesgoAsociado((int)(riesgo * 100));
                                seleccionado.setGananciaObtenida(ganancia);

                                cultivosUsados.put(nombreCultivo, cultivosUsados.getOrDefault(nombreCultivo, 0) + 1);
                                marcarParcelas(i, j, N, M, parcelasUsadas, true);
                                configuracionActual.add(seleccionado);

                                // Llamada recursiva al siguiente nivel de backtracking
                                backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                                        gananciaActual + ganancia, parcelasUsadas, riesgos,
                                        gananciaMaximaPorParcela, cultivosUsados, cultivoMultiple);

                                // Deshacer los cambios (backtracking)
                                configuracionActual.remove(configuracionActual.size() - 1);
                                marcarParcelas(i, j, N, M, parcelasUsadas, false);
                                cultivosUsados.put(nombreCultivo, cultivosUsados.get(nombreCultivo) - 1);
                            }
                        }
                    }
                }
            }
        }

        // Intentar sin colocar el cultivo actual
        backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                gananciaActual, parcelasUsadas, riesgos, gananciaMaximaPorParcela, cultivosUsados, cultivoMultiple);
    }


    private double calcularGananciaMaximaPorParcela(List<Cultivo> cultivos) {
        double gananciaMaxima = 0;
        for (Cultivo cultivo : cultivos) {
            double gananciaParcela = cultivo.getPrecioDeVentaPorParcela() - cultivo.getCostoPorParcela();
            gananciaMaxima = Math.max(gananciaMaxima, gananciaParcela);
        }
        return gananciaMaxima;
    }

    private int contarParcelasLibres(boolean[][] parcelasUsadas) {
        int libres = 0;
        for (int i = 0; i < CAMPO_SIZE; i++) {
            for (int j = 0; j < CAMPO_SIZE; j++) {
                if (!parcelasUsadas[i][j]) {
                    libres++;
                }
            }
        }
        return libres;
    }


    private boolean esValida(int fila, int columna, int alto, int ancho, boolean[][] parcelasUsadas) {
        if (fila + alto > CAMPO_SIZE || columna + ancho > CAMPO_SIZE) {
            return false;
        }
        // Verificar restricción flexible, puedes parametrizar el valor 11 o eliminarlo si no aplica.
        if (alto + ancho > 11) {  // Considera hacerlo configurable.
            return false;
        }
        // Verificar si hay parcelas ocupadas
        for (int i = fila; i < fila + alto; i++) {
            for (int j = columna; j < columna + ancho; j++) {
                if (parcelasUsadas[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }


    private double calcularGanancia(int fila, int columna, int dimensionX, int dimensionY, Cultivo cultivo, double[][] riesgos) {
        double gananciaTotal = 0;
        for (int i = fila; i < fila + dimensionX; i++) {
            for (int j = columna; j < columna + dimensionY; j++) {
                double riesgo = riesgos[i][j];
                gananciaTotal += (1 - riesgo) * (cultivo.getPrecioDeVentaPorParcela() - cultivo.getCostoPorParcela());
            }
        }
        return gananciaTotal - cultivo.getInversionRequerida();
    }

    private double calcularRiesgo(int fila, int columna, int dimensionX, int dimensionY, double[][] riesgos) {
        double riesgoTotal = 0;
        int numParcelas = dimensionX * dimensionY;
        for (int i = fila; i < fila + dimensionX; i++) {
            for (int j = columna; j < columna + dimensionY; j++) {
                riesgoTotal += riesgos[i][j];
            }
        }
        return riesgoTotal / numParcelas;
    }


    private void marcarParcelas(int fila, int columna, int alto, int ancho,
                                boolean[][] parcelasUsadas, boolean valor) {
        for (int i = fila; i < fila + alto; i++) {
            for (int j = columna; j < columna + ancho; j++) {
                parcelasUsadas[i][j] = valor;
            }
        }
    }
}