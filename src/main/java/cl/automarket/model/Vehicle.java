package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
@Document(collection = "vehiculos")
public class Vehicle {
    @Id public String id;
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
}
