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
    private double mejorGanancia = Double.NEGATIVE_INFINITY;
    private List<CultivoSeleccionado> mejorConfiguracion = new ArrayList<>();
    private static final int CAMPO_SIZE = 50;
    private static final int DIMENSION = 5; // Dimensión fija 5x5
    private String cultivoMultiple;

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
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

        // Seleccionar el cultivo que puede plantarse múltiples veces
        if (!cultivosTemporada.isEmpty()) {
            cultivoMultiple = cultivosTemporada.get(0).getNombre();
        }

        double gananciaMaximaPorParcela = calcularGananciaMaximaPorParcela(cultivosTemporada);
        Map<String, Integer> cultivosUsados = new HashMap<>();
        backtracking(cultivosTemporada, new ArrayList<>(), 0, 0, parcelasUsadas, riesgos,
                gananciaMaximaPorParcela, cultivosUsados);

        return mejorConfiguracion;
    }

    private void backtracking(List<Cultivo> cultivos, List<CultivoSeleccionado> configuracionActual,
                              int indiceCultivo, double gananciaActual, boolean[][] parcelasUsadas,
                              double[][] riesgos, double gananciaMaximaPorParcela,
                              Map<String, Integer> cultivosUsados) {

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

        // Verificar si el cultivo ya ha sido usado (excepto el cultivoMultiple)
        if (!nombreCultivo.equals(cultivoMultiple) &&
                cultivosUsados.getOrDefault(nombreCultivo, 0) > 0) {
            // Si ya se usó este cultivo y no es el cultivoMultiple, pasar al siguiente
            backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                    gananciaActual, parcelasUsadas, riesgos, gananciaMaximaPorParcela, cultivosUsados);
            return;
        }

        // Intentar colocar el cultivo en diferentes posiciones
        for (int i = 0; i < CAMPO_SIZE - DIMENSION + 1; i++) {
            for (int j = 0; j < CAMPO_SIZE - DIMENSION + 1; j++) {
                if (esValida(i, j, DIMENSION, DIMENSION, parcelasUsadas)) {
                    // Calcular valores para el cultivo en esta posición
                    double ganancia = calcularGanancia(i, j, DIMENSION, DIMENSION, cultivoActual, riesgos);
                    double riesgo = calcularRiesgo(i, j, DIMENSION, DIMENSION, riesgos);

                    // Crear el cultivo seleccionado
                    CultivoSeleccionado seleccionado = new CultivoSeleccionado();
                    seleccionado.setNombreCultivo(nombreCultivo);
                    seleccionado.setEsquinaSuperiorIzquierda(new Coordenada(i, j));
                    seleccionado.setEsquinaInferiorDerecha(new Coordenada(i + DIMENSION - 1, j + DIMENSION - 1));
                    seleccionado.setMontoInvertido(
                            cultivoActual.getInversionRequerida() +
                                    (cultivoActual.getCostoPorParcela() * DIMENSION * DIMENSION)
                    );
                    seleccionado.setRiesgoAsociado((int)(riesgo * 100));
                    seleccionado.setGananciaObtenida(ganancia);

                    // Actualizar contador de cultivos usados
                    cultivosUsados.put(nombreCultivo,
                            cultivosUsados.getOrDefault(nombreCultivo, 0) + 1);

                    // Marcar parcelas como usadas
                    marcarParcelas(i, j, DIMENSION, DIMENSION, parcelasUsadas, true);
                    configuracionActual.add(seleccionado);

                    // Llamada recursiva
                    backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                            gananciaActual + ganancia, parcelasUsadas, riesgos,
                            gananciaMaximaPorParcela, cultivosUsados);

                    // Deshacer cambios (backtracking)
                    configuracionActual.remove(configuracionActual.size() - 1);
                    marcarParcelas(i, j, DIMENSION, DIMENSION, parcelasUsadas, false);
                    // Deshacer el contador de cultivos usados
                    cultivosUsados.put(nombreCultivo,
                            cultivosUsados.get(nombreCultivo) - 1);
                }
            }
        }

        // Probar sin colocar el cultivo actual
        backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                gananciaActual, parcelasUsadas, riesgos, gananciaMaximaPorParcela, cultivosUsados);
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
        // Verificar límites del campo
        if (fila + alto > CAMPO_SIZE || columna + ancho > CAMPO_SIZE) {
            return false;
        }

        // Verificar restricción N + M ≤ 11
        if (alto + ancho > 11) {
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

    private double calcularGanancia(int fila, int columna, int alto, int ancho,
                                    Cultivo cultivo, double[][] riesgos) {
        double gananciaTotal = 0;

        for (int i = fila; i < fila + alto; i++) {
            for (int j = columna; j < columna + ancho; j++) {
                double riesgo = riesgos[i][j];
                gananciaTotal += (1 - riesgo) *
                        (cultivo.getPrecioDeVentaPorParcela() - cultivo.getCostoPorParcela());
            }
        }

        return gananciaTotal - cultivo.getInversionRequerida();
    }

    private double calcularRiesgo(int fila, int columna, int alto, int ancho, double[][] riesgos) {
        double riesgoTotal = 0;
        int numParcelas = alto * ancho;

        for (int i = fila; i < fila + alto; i++) {
            for (int j = columna; j < columna + ancho; j++) {
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