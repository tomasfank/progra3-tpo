package org.example.interfaces;

import org.example.classes.Cultivo;
import org.example.classes.CultivoSeleccionado;

import java.util.List;

public interface PlanificarCultivos {
    List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> var1, double[][] var2, String var3);
}
