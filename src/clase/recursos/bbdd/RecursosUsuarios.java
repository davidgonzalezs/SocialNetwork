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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import com.sun.org.apache.xerces.internal.util.URI;

import clase.datos.Link;
import clase.datos.Solicitud;
import clase.datos.Usuario;
import clase.datos.Usuarios;

@Path("/usuario")
public class RecursosUsuarios {

	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;

	public RecursosUsuarios() {
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

	// Lista de usuarios /JSON generada con listas en JAXB
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response getUsuarios1() {
		try {
			String sql = "SELECT * FROM SocialUPM.Usuario ;";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			Usuarios u = new Usuarios();
			ArrayList<Link> usuarios = u.getUsuarios();
			rs.beforeFirst();
			while (rs.next()) {
				usuarios.add(new Link(uriInfo.getAbsolutePath() + "/" + rs.getInt("idUser"),"self"));
			}
			return Response.status(Response.Status.OK).entity(u).build();
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	//Datos de un usuario en concreto
	
	@GET
	@Path("{usuario_id}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getUsuario2(@PathParam("usuario_id") String id) {
		try {
			int int_id = Integer.parseInt(id);
			String sql = "SELECT * FROM Usuario where idUser=" + int_id + ";";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				Usuario usuario =  usuarioFromRS(rs);
				return Response.ok().entity(usuario).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();
			}
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No puedo parsear a entero").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	//Buscar lista de amigos por su nombre
	@GET
	@Path("/buscar")
	@Produces({MediaType.APPLICATION_JSON})
	public Response buscarAmigos(@QueryParam("cadena") String cadena) {
		try {
			String sql = "SELECT * FROM SocialUPM.Usuario where nombre like \"%"+ cadena +"%\";";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			Usuarios u = new Usuarios();
			ArrayList<Link> usuarios = u.getUsuarios();
			rs.beforeFirst();
			while (rs.next()) {
				usuarios.add(new Link("http://localhost:8080/SocialUPM/api/usuario/" + rs.getInt("idUser"),"self"));
			}
			return Response.status(Response.Status.OK).entity(u).build();
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	//Obtener una lista de todos nuestros amigos y filtrarla por nombre o limitar cantidad de informacion
	@GET
	@Path("/listaAmigos/{usuario_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listaUsuarios(@PathParam("usuario_id") String id,
			@QueryParam("nombre") @DefaultValue("0") String nombre,
			@QueryParam("limInf") @DefaultValue("0") String limInf,
			@QueryParam("limSup") @DefaultValue("0") String limSup,
			@QueryParam("num") @DefaultValue("0") String num) {
		try {
			int id1 = Integer.parseInt(id);
			int limInf1 = Integer.parseInt(limInf);
			int limSup1 = Integer.parseInt(limSup);
			int num1 = Integer.parseInt(num);
			
			//lista con todos los amigos al no haber metido ningun parametro
			if(nombre.equals("0") && limInf1==0 && limSup1==0 && num1==0) {
				String sql = "SELECT idUser,nombre FROM SocialUPM.Usuario U,SocialUPM.Amistad A where A.idUsuarioPidePet = "+ id1 + " and A.idUsuarioRecibePet = U.idUser;";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				Usuarios u = new Usuarios();
				ArrayList<Link> usuarios = u.getUsuarios();
				rs.beforeFirst();
				while (rs.next()) {
					usuarios.add(new Link("http://localhost:8080/SocialUPM/api/usuario/" + rs.getInt("idUser"),"self"));
				}
				return Response.status(Response.Status.OK).entity(u).build();
			}
			//lista con todos los amigos con un determinado nombre
			else if (!nombre.equals("0") && limInf1==0 && limSup1==0 && num1==0){
				String sql = "SELECT idUser,nombre FROM SocialUPM.Usuario U,SocialUPM.Amistad A where A.idUsuarioPidePet = "+ id1 + " and A.idUsuarioRecibePet = U.idUser and U.nombre like '%"+nombre+"%';";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				Usuarios u = new Usuarios();
				ArrayList<Link> usuarios = u.getUsuarios();
				rs.beforeFirst();
				while (rs.next()) {
					usuarios.add(new Link("http://localhost:8080/SocialUPM/api/usuario/" + rs.getInt("idUser"),"self"));
				}
				return Response.status(Response.Status.OK).entity(u).build();
			}
			//lista con todos los amigos entre dos limites
			else if(nombre.equals("0") && limInf1!=0 && limSup1!=0 && num1==0) {
				String sql = "SELECT idUser,nombre FROM SocialUPM.Usuario U,SocialUPM.Amistad A where A.idUsuarioPidePet = "+ id1 + 
						" and A.idUsuarioRecibePet = U.idUser limit "+ limInf1 + ","+ limSup1 +";";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				Usuarios u = new Usuarios();
				ArrayList<Link> usuarios = u.getUsuarios();
				rs.beforeFirst();
				while (rs.next()) {
					usuarios.add(new Link("http://localhost:8080/SocialUPM/api/usuario/" + rs.getInt("idUser"),"self"));
				}
				return Response.status(Response.Status.OK).entity(u).build();
			}
			//lista de los n primeros amigos
			else if(nombre.equals("0") && limInf1==0 && limSup1==0 && num1!=0) {
				String sql = "SELECT idUser,nombre FROM SocialUPM.Usuario U,SocialUPM.Amistad A where A.idUsuarioPidePet = "+ id1 + 
						" and A.idUsuarioRecibePet = U.idUser limit "+ num1 + ";";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				Usuarios u = new Usuarios();
				ArrayList<Link> usuarios = u.getUsuarios();
				rs.beforeFirst();
				while (rs.next()) {
					usuarios.add(new Link("http://localhost:8080/SocialUPM/api/usuario/" + rs.getInt("idUser"),"self"));
				}
				return Response.status(Response.Status.OK).entity(u).build();
			}
			//no se contempla cualquier otro caso
			else {
				return Response.status(Response.Status.OK).entity("Los parametros no han sido introducidos correctamente.").build();
			}
			
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	//Creacion de un nuevo usuario en la bbdd
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUsuario(Usuario usuario) {
		try {
			String sql = "INSERT INTO SocialUPM.Usuario (nombre,pais,descripcion) VALUES ('" + usuario.getnombre() +"','" + usuario.getPais() +"','" + usuario.getDescripcion() +"' );";
			PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			int affectedRows = ps.executeUpdate();
			
			// Obtener el ID del elemento recién creado. 
			// Necesita haber indicado Statement.RETURN_GENERATED_KEYS al ejecutar un statement.executeUpdate() o al crear un PreparedStatement
			ResultSet generatedID = ps.getGeneratedKeys();
			if (generatedID.next()) {
				usuario.setId(generatedID.getInt(1));
				String location = uriInfo.getAbsolutePath() + "/" + usuario.getId();
				return Response.status(Response.Status.CREATED).entity(usuario).header("Location", location).header("Content-Location", location).build();
			}
			return Response.status(Response.Status.CREATED).build();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el usuario\n" + e.getStackTrace()).build();
		}
	}
	
	//Modificar la informacion de un usuario (su descripcion personal y el pais)
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{usuario_id}")
	public Response updateGaraje(@PathParam("usuario_id") String id, Usuario nuevoUsuario) {
		try {
			Usuario usuario;
			int int_id = Integer.parseInt(id);
			String sql = "SELECT * FROM SocialUPM.Usuario where idUser=" + int_id + ";";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				usuario =  usuarioFromRS(rs);
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();
			}
			usuario.setPais(nuevoUsuario.getPais());
			usuario.setDescripcion(nuevoUsuario.getDescripcion());

			sql = "UPDATE SocialUPM.Usuario SET pais='" + usuario.getPais()+ "', descripcion='" + usuario.getDescripcion() + "' where Usuario.idUser="+int_id+";";
			ps = conn.prepareStatement(sql);
			int affectedRows = ps.executeUpdate();
			
			// Location a partir del URI base (host + root de la aplicación + ruta del servlet)
			String location = uriInfo.getBaseUri() + "usuario/" + usuario.getId();
			return Response.status(Response.Status.OK).entity(usuario).header("Content-Location", location).build();			
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo actualizar el usuario\n" + e.getStackTrace()).build();
		}
	}
	
	//Eliminacion de un usuario de la bbdd

	@DELETE
	@Path("{usuario_id}")
	public Response deleteUsuario(@PathParam("usuario_id") String id) {
		try {
			Usuario usuario;
			int int_id = Integer.parseInt(id);
			String sql = "DELETE FROM SocialUPM.Usuario WHERE idUser=" + int_id + ";";
			PreparedStatement ps = conn.prepareStatement(sql);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.NO_CONTENT).build();
			else 
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();		
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar el usuario\n" + e.getStackTrace()).build();
		}
	}
	
	
	private Usuario usuarioFromRS(ResultSet rs) throws SQLException {
		Usuario usuario = new Usuario(rs.getString("nombre"),rs.getString("pais"),rs.getString("descripcion"));
		usuario.setId(rs.getInt("idUser"));
		return usuario;
	}
}
