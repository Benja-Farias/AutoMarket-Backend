package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "vehiculos")
public class Vehicle {
    @Id
    public String id;

    // Campos utilizados en el catálogo público actual.
    public String marca;
    public String modelo;
    public Integer anio;
    public String transmision;
    public Integer km;
    public String combustible;
    public BigDecimal precio;
    public String estado;
    public String imagen = "auto1";
    public Boolean destacado = false;

    // Datos adicionales procedentes del modelo EP3 de Taller de Base de Datos.
    @Indexed(unique = true, sparse = true)
    public String vin;
    public String color;
    public String tipoVehiculo;
    public Integer idSucursal;
    public String estadoInventario;
}
