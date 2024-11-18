package org.example.implementation;

import org.example.classes.Cultivo;
import org.example.classes.CultivoSeleccionado;
import org.example.interfaces.PlanificarCultivos;

import java.util.List;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {

    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        // TODO
        return null;
    }
}