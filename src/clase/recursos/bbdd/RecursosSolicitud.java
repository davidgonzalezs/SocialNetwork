package clase.recursos.bbdd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.naming.NamingContext;

import com.mysql.jdbc.Statement;

import clase.datos.Link;
import clase.datos.Solicitud;
import clase.datos.Usuario;
import clase.datos.Usuarios;

@Path("/solicitud")
public class RecursosSolicitud {
	
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

	public RecursosSolicitud() {
		
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			NamingContext envCtx = (NamingContext) ctx.lookup("java:comp/env");
			
			ds = (DataSource) envCtx.lookup("jdbc/SocialUPM");

			conn = ds.getConnection();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//Metodo post para añadir un amigo
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response anadirAmigo(Solicitud sol) {
		try {
			//Se comprueba que el usuario que solicita la amistad existe
			String selectSqlUsPide= "select * from SocialUPM.Usuario where idUser= " + sol.getIdUsuarioPidePet() + ";";
			PreparedStatement psUsPide = conn.prepareStatement(selectSqlUsPide);
			ResultSet rsUsPide = psUsPide.executeQuery();
			if(!rsUsPide.next()) {
				return Response.status(Response.Status.CREATED).entity("El usuario que envia la solicitud de amistad con id " + sol.getIdUsuarioPidePet() + " no existe.").build();
			}
			
			//Se comprueba que el usuario que recibe la amistad existe
			String selectSqlUsRecibe= "select * from SocialUPM.Usuario where idUser= " + sol.getIdUsuarioRecibePet() + ";";
			PreparedStatement psUsRecibe = conn.prepareStatement(selectSqlUsRecibe);
			ResultSet rsUsRecibe = psUsRecibe.executeQuery();
			if(!rsUsRecibe.next()) {
				return Response.status(Response.Status.CREATED).entity("El usuario que recibe la solicitud de amistad con id " + sol.getIdUsuarioRecibePet() + " no existe.").build();
			}
			
			//Se comprueba que un usuario no se mande una peticion de amistad a si mismo
			if(sol.getIdUsuarioPidePet()==sol.getIdUsuarioRecibePet()) {
				return Response.status(Response.Status.CREATED).entity("El usuario " + sol.getIdUsuarioPidePet() + " no puede hacerse amigo de sí mismo.").build();
			}
			
			//Se comprueba que la los dos usuarios no sean amigos con anterioridad
			String selectSql= "select * from SocialUPM.Amistad where idUsuarioPidePet = " + sol.getIdUsuarioPidePet() + " and idUsuarioRecibePet = " + sol.getIdUsuarioRecibePet() + ";";
			PreparedStatement ps1 = conn.prepareStatement(selectSql);
			ResultSet rs = ps1.executeQuery();
			
			if(!rs.next()) {
				
				String insertSql = "insert into SocialUPM.Amistad (idUsuarioPidePet, idUsuarioRecibePet) values (" + sol.getIdUsuarioPidePet() +", " + sol.getIdUsuarioRecibePet() + " );";
				PreparedStatement ps2 = conn.prepareStatement(insertSql,Statement.RETURN_GENERATED_KEYS);
				int affectedRows2 = ps2.executeUpdate();
				
				return Response.status(Response.Status.CREATED).entity("El usuario " + sol.getIdUsuarioPidePet() + " se ha hecho amigo del usuario " + sol.getIdUsuarioRecibePet()).build();
			}
			else {
				return Response.status(Response.Status.CREATED).entity("La relacion de amistad entre el usuario " + sol.getIdUsuarioPidePet() + " y el usuario " + sol.getIdUsuarioRecibePet() + " ya existe.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo realizar la solicitud\n" + e.getStackTrace()).build();
		}
	}
	
	//Eliminar una amistad
	@DELETE
	@Path("{id_Amistad}")
	public Response deleteAmistad(@PathParam("id_Amistad") String id) {
		try {
			int int_id = Integer.parseInt(id);
			String sql = "DELETE FROM SocialUPM.Amistad WHERE idAmistad=" + int_id + ";";
			PreparedStatement ps = conn.prepareStatement(sql);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.NO_CONTENT).build();
			else 
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();		
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar la amistad.\n" + e.getStackTrace()).build();
		}
	}
	
	private Solicitud solicitudFromRS(ResultSet rs) throws SQLException {
		Solicitud solicitud = new Solicitud(rs.getInt("idUsuarioPidePet"),rs.getInt("idUsuarioRecibePet"));
		solicitud.setIdAmistad(rs.getInt("idAmistad"));
		return solicitud;
	}
}
