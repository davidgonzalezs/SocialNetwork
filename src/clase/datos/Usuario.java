package clase.datos;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "usuario")
public class Usuario {
    private int id;
    private String nombre;
    private String pais;
    private String descripcion;

    public String getPais() {
		return pais;
	}

	public void setPais(String pais) {
		this.pais = pais;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Usuario() {
    
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public String getnombre() {
		return nombre;
	}

	public void setnombre(String nombre) {
		this.nombre = nombre;
	}

	public Usuario(String nombre,String pais, String descripcion) {
		super();
	    this.id = id;
	    this.nombre=nombre;
	    this.pais=pais;
	    this.descripcion=descripcion;
	}
}
