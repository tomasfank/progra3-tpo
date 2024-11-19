package org.example.implementation;

import org.example.classes.Cultivo;
import org.example.classes.CultivoSeleccionado;
import org.example.classes.Coordenada;
import org.example.interfaces.PlanificarCultivos;

import java.util.ArrayList;
import java.util.List;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {
    private double mejorGanancia = Double.NEGATIVE_INFINITY;
    private List<CultivoSeleccionado> mejorConfiguracion = new ArrayList<>();
    private static final int TAMAÑO_CAMPO = 100;

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        boolean[][] parcelasUsadas = new boolean[TAMAÑO_CAMPO][TAMAÑO_CAMPO];

        // Filtrar cultivos por temporada
        List<Cultivo> cultivosTemporada = new ArrayList<>();
        for (Cultivo cultivo : cultivosDisponibles) {
            if (cultivo.getTemporadaOptima().equals(temporada)) {
                cultivosTemporada.add(cultivo);
            }
        }

        backtracking(cultivosTemporada, new ArrayList<>(), 0, 0, parcelasUsadas, riesgos);

        return mejorConfiguracion;
    }

    private void backtracking(List<Cultivo> cultivos, List<CultivoSeleccionado> configuracionActual,
                              int indiceCultivo, double gananciaActual, boolean[][] parcelasUsadas,
                              double[][] riesgos) {
        if (indiceCultivo >= cultivos.size()) {
            if (gananciaActual > mejorGanancia) {
                mejorGanancia = gananciaActual;
                mejorConfiguracion = new ArrayList<>(configuracionActual);
            }
            return;
        }

        Cultivo cultivoActual = cultivos.get(indiceCultivo);

        // Intentar colocar el cultivo en diferentes posiciones
        for (int i = 0; i < TAMAÑO_CAMPO - 5; i++) {
            for (int j = 0; j < TAMAÑO_CAMPO - 6; j++) {
                if (esValida(i, j, 5, 6, parcelasUsadas)) {  // Usando dimensiones de ejemplo 5x6
                    // Calcular valores para el cultivo en esta posición
                    double ganancia = calcularGanancia(i, j, 5, 6, cultivoActual, riesgos);
                    double riesgo = calcularRiesgo(i, j, 5, 6, riesgos);

                    // Crear el cultivo seleccionado
                    CultivoSeleccionado seleccionado = new CultivoSeleccionado();
                    seleccionado.setNombreCultivo(cultivoActual.getNombre());
                    seleccionado.setEsquinaSuperiorIzquierda(new Coordenada(i, j));
                    seleccionado.setEsquinaInferiorDerecha(new Coordenada(i + 4, j + 5));
                    seleccionado.setMontoInvertido(
                            cultivoActual.getInversionRequerida() +
                                    (cultivoActual.getCostoPorParcela() * 5 * 6)
                    );
                    seleccionado.setRiesgoAsociado((int)(riesgo * 100));
                    seleccionado.setGananciaObtenida(ganancia);

                    // Marcar parcelas como usadas
                    marcarParcelas(i, j, 5, 6, parcelasUsadas, true);
                    configuracionActual.add(seleccionado);

                    // Llamada recursiva
                    backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                            gananciaActual + ganancia, parcelasUsadas, riesgos);

                    // Deshacer cambios (backtracking)
                    configuracionActual.remove(configuracionActual.size() - 1);
                    marcarParcelas(i, j, 5, 6, parcelasUsadas, false);
                }
            }
        }

        // Probar sin colocar el cultivo actual
        backtracking(cultivos, configuracionActual, indiceCultivo + 1,
                gananciaActual, parcelasUsadas, riesgos);
    }

    private boolean esValida(int fila, int columna, int alto, int ancho, boolean[][] parcelasUsadas) {
        // Verificar límites del campo
        if (fila + alto > TAMAÑO_CAMPO || columna + ancho > TAMAÑO_CAMPO) {
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