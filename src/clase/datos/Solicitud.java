package clase.datos;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="solicitud")
public class Solicitud {
    private int idAmistad;
    private int idUsuarioPidePet;
    private int idUsuarioRecibePet;

    public Solicitud() {
    
    }
	
	public int getIdAmistad() {
		return idAmistad;
	}


	public void setIdAmistad(int idAmistad) {
		this.idAmistad = idAmistad;
	}


	public int getIdUsuarioPidePet() {
		return idUsuarioPidePet;
	}


	public void setIdUsuarioPidePet(int idUsuarioPidePet) {
		this.idUsuarioPidePet = idUsuarioPidePet;
	}


	public int getIdUsuarioRecibePet() {
		return idUsuarioRecibePet;
	}


	public void setIdUsuarioRecibePet(int idUsuarioRecibePet) {
		this.idUsuarioRecibePet = idUsuarioRecibePet;
	}

	public Solicitud(int idUsuarioPidePet, int idUsuarioRecibePet) {
		this.idUsuarioPidePet=idUsuarioPidePet;
		this.idUsuarioRecibePet=idUsuarioRecibePet;
	}
}
