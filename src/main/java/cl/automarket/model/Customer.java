package cl.automarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "clientes")
public class Customer {
    @Id
    public String id;

    // Identificador de cliente utilizado en AutoMarket (RUT en el caso EP1).
    @Indexed(unique = true)
    public String idCliente;

    public String nombre;
    public String apellido;
    public String correoElectronico;
    public String telefono;
    public String direccion;
}
