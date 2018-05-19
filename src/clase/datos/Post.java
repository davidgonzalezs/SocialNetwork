package clase.datos;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Post {
	private int id_Post;
	private String fecha;
	private String mensaje; //max length 300
	private int id_Propietario;
	
	public Post() {
		
	}
	
	public Post(int id_Post, String mensaje, String fecha, int id_Propietario) {
	
		this.id_Post = id_Post;
		this.fecha = fecha;
		this.mensaje = mensaje;
		this.id_Propietario = id_Propietario;
	}
	
	public int getId_Post() {
		return id_Post;
	}
	
	public void setId_Post(int id_Post) {
		this.id_Post = id_Post;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getContenido() {
		return mensaje;
	}
	public void setContenido(String contenido) {
		this.mensaje = contenido;
	}
	public int getId_Propietario() {
		return id_Propietario;
	}
	public void setId_Propietario(int id_Propietario) {
		this.id_Propietario = id_Propietario;
	}
	

}