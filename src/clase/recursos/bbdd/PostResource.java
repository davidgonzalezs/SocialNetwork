package clase.recursos.bbdd;

import java.sql.Connection;

import java.lang.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

import clase.datos.Post;

@Path("/posts")
public class PostResource {
	@Context
	private UriInfo uriInfo;

	private DataSource ds;
	private Connection conn;
	
	public PostResource() {
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
	
	@Path("getpost/{post_id}")
	@GET
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getPost(@PathParam("post_id") String id) throws Exception {
		try {
			int id_post = Integer.parseInt(id);
			String postQuery = "SELECT * FROM SocialUPM.Post WHERE idPost = " + id_post + ";";
			PreparedStatement ps = conn.prepareStatement(postQuery);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				Post post = postFromRS(rs);
				return Response.status(Response.Status.OK).entity(post).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();
			}
		} catch (NumberFormatException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("No puedo parsear a entero").build();
		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response publicarPost(Post post) {
		try {
			
			String sql = "INSERT INTO SocialUPM.Post (`Message`, `Date`,`idPropietario`) VALUES ('"
					+ post.getContenido() + "', '" + post.getFecha() + "' , " + post.getId_Propietario() + ");";
			PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			int affectedRows = ps.executeUpdate();
			
			// Obtener el ID del elemento recién creado. 
			// Necesita haber indicado Statement.RETURN_GENERATED_KEYS al ejecutar un statement.executeUpdate() o al crear un PreparedStatement
			ResultSet generatedID = ps.getGeneratedKeys();
			if (generatedID.next()) {
				int x = generatedID.getInt(1);
				post.setId_Post(x);
				String location = uriInfo.getAbsolutePath() + "/" + post.getId_Post();
				return Response.status(Response.Status.CREATED).entity(post).header("Location", location).header("Content-Location", location).build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el post").build();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo crear el post\n" + e.getStackTrace()).build();
		}

	}
	
	@DELETE
	@Path("{post_id}")
	public Response deletePost(@PathParam("post_id") String id) {
		try {
			Post post;
			int int_id = Integer.parseInt(id);
			String sql = "DELETE FROM `SocialUPM`.`Post` WHERE `idPost`='" + int_id + "';";
			PreparedStatement ps = conn.prepareStatement(sql);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 1)
				return Response.status(Response.Status.OK).build();
			else 
				return Response.status(Response.Status.NOT_FOUND).entity("Elemento no encontrado").build();		
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No se pudo eliminar el garaje\n" + e.getStackTrace()).build();
		}
	}
	
	@GET
	@Path("/bydate/{id_Propietario}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPostsByDate(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("fecha") String fecha) {
		try {
			int idProp = Integer.parseInt(idPropietario);
			String sql = "SELECT * FROM SocialUPM.Post WHERE Date = '" + fecha + "' AND idPropietario = " + idProp + ";";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			String listado = "<?xml version=\"1.0\"?>" + "<posts>";
			rs.beforeFirst();
			while (rs.next()) {
				listado = listado + "<post href=\"" + uriInfo.getAbsolutePath() + "/" + rs.getInt("idPost") + "\" />";
			}
			listado = listado + "</posts>";
			return Response.status(Response.Status.OK).entity(listado).build();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	
	// Limites izquierdo y derecho tipo [izq,der] para devolver los post entre los limites
	@GET
	@Path("getpostsid/{id_Propietario}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPostsByNumber(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("numero") @DefaultValue("0") String numero, @QueryParam("limIzq") @DefaultValue("0") String limIzq, 
			@QueryParam("limDer") @DefaultValue("0") String limDer) {
		try {
			int number = Integer.parseInt(numero);
			int limI = Integer.parseInt(limIzq);
			int limD = Integer.parseInt(limDer);
			if (limI != 0 && limD != 0) {
				String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + ";";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				String listado = "<?xml version=\"1.0\"?>" + "<posts>";
				rs.beforeFirst();
				int cont = 1;
				while (rs.next()) {
					if(cont >= limI && cont <= limD) {
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/getpost/" + rs.getInt("idPost") + "\" />";
					}
					cont++;
				}
				listado = listado + "</posts>";
				return Response.status(Response.Status.OK).entity(listado).build();
			}else if(number != 0) {
				String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + " LIMIT " + numero;
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				String listado = "<?xml version=\"1.0\"?>" + "<posts>";
				rs.beforeFirst();
				while (rs.next()) {
					listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/getpost/" + rs.getInt("idPost") + "\" />";
				}
				listado = listado + "</posts>";
				return Response.status(Response.Status.OK).entity(listado).build();
			}else {
				String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario +";";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				String listado = "<?xml version=\"1.0\"?>" + "<posts>";
				rs.beforeFirst();
				while (rs.next()) {
					listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/getpost/" + rs.getInt("idPost") + "\" />";
				}
				listado = listado + "</posts>";
				return Response.status(Response.Status.OK).entity(listado).build();
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@GET
	@Path("bycontent/{id_Propietario}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPostByContent(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("contenido") @DefaultValue("0") String content,
			@QueryParam("numero") @DefaultValue("0") String numero,
			@QueryParam("limIzq") @DefaultValue("0") String limIzq, 
			@QueryParam("limDer") @DefaultValue("0") String limDer) throws ParseException {
		ArrayList<Integer> misAmigos = new ArrayList<Integer>();
		try {
			String sql = "SELECT * FROM SocialUPM.Amistad WHERE idUsuario1 = " + idPropietario;
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while(rs.next()) {
				misAmigos.add(rs.getInt("idUsuario2"));
			}
		}catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD de amigos").build();
		}
		
		int num = Integer.parseInt(numero);
		int limI = Integer.parseInt(limIzq);
		int limD =  Integer.parseInt(limDer);
		int cont = 1;
		String listado = "<?xml version=\"1.0\"?>" + "<posts>";
		for(int x=0;x<misAmigos.size();x++) {
			int idAmigo = misAmigos.get(x);
			try {
				if(num != 0 && limI == 0 && limD==0) {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo + " AND Message LIKE '%" + content + "%' LIMIT " + num + ";";
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()&&num!=0) {
						num--;
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
					}
					if (num == 0) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
				}else if(num == 0 && limI != 0 && limD!=0) {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo + " AND Message LIKE '%" + content + "%';";
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()) {
						if(cont >= limI && cont <= limD) {
							listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
						}
						cont++;
					}
					
					if(cont>limD) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
					
				}else {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo + " AND Message LIKE '%" + content + "%';";
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()) {
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
					}
						
				}
			}catch (SQLException e) {
				e.printStackTrace();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
			}
		
		}
		listado+="</posts>";
		return Response.status(Response.Status.OK).entity(listado).build();
		
		}
	
	@GET
	@Path("byFriendsAndDate/{id_Propietario}")
	@Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getFriendPosts(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("fechaLimite") @DefaultValue("0") String date,
			@QueryParam("numero") @DefaultValue("0") String numero,
			@QueryParam("limIzq") @DefaultValue("0") String limIzq, 
			@QueryParam("limDer") @DefaultValue("0") String limDer) throws ParseException {
		ArrayList<Integer> misAmigos = new ArrayList<Integer>();
		try {
			String sql = "SELECT * FROM SocialUPM.Amistad WHERE idUsuario1 = " + idPropietario;
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.beforeFirst();
			while(rs.next()) {
				misAmigos.add(rs.getInt("idUsuario2"));
			}
			System.out.println(misAmigos);
		}catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD de amigos").build();
		}
	
		int num = Integer.parseInt(numero);
		int limI = Integer.parseInt(limIzq);
		int limD =  Integer.parseInt(limDer);
		int cont = 1;
		String listado = "<?xml version=\"1.0\"?>" + "<posts>";
		for(int x=0;x<misAmigos.size();x++) {
			int idAmigo = misAmigos.get(x);
			if(num!=0 && limI == 0 && limD==0 && !date.contentEquals("0")) { //por fecha limite numero
				try {
					
					String sql = "SELECT * FROM SocialUPM.Post WHERE Date <= '" + date + "' AND idPropietario = " + idAmigo + " LIMIT " + num +";" ;
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while(rs.next()) {
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
						num--;
					}
					
					if(num==0) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
					
				}catch (SQLException e) {
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD de amigos").build();
				}
			}else if(num==0 && limI != 0 && limD!=0 && !date.contentEquals("0")) {
				try {
					String sql = "SELECT * FROM SocialUPM.Post WHERE Date <= '" + date + "' AND idPropietario = " + idAmigo +";" ;
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()) {
						if(cont >= limI && cont <= limD) {
							listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
						}
						cont++;
					}
					
					if(cont>limD) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
				}catch (SQLException e) {
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
				}
			}else if(num!=0 && limI == 0 && limD==0 && date.contentEquals("0")) {
				try {
					
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo + " LIMIT " + num +";" ;
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while(rs.next()) {
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
						num--;
					}
					
					if(num==0) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
					
				}catch (SQLException e) {
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD de amigos").build();
				}
				
			}else if(num==0 && limI != 0 && limD!=0 && date.contentEquals("0")) {
				try {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo +";" ;
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()) {
						if(cont >= limI && cont <= limD) {
							listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
						}
						cont++;
					}
					
					if(cont>limD) {
						listado+="</posts>";
						return Response.status(Response.Status.OK).entity(listado).build();
					}
				}catch (SQLException e) {
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
				}
				
			}else {
				try {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idAmigo +";" ;
					PreparedStatement ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					rs.beforeFirst();
					while (rs.next()) {
						listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" +rs.getInt("idPost") + "\" />";
					}
					
				}catch (SQLException e) {
					e.printStackTrace();
					return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
				}
			}
		}
		listado+="</posts>";	
		return Response.status(Response.Status.OK).entity(listado).build();
		
	}
		
	
	@GET
	@Path("count/{id_Propietario}")
	@Produces(MediaType.APPLICATION_XML)
	public Response countMyPosts(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("fechaIni") @DefaultValue("0") String fechaIni, 
			@QueryParam("fechaFin") @DefaultValue("0") String fechaFin) throws ParseException {
		try {
			int secsIni[] = getFechaInSecs(fechaIni);
			int secsFin[] = getFechaInSecs(fechaFin);
			int difAño = secsIni[1]-secsFin[1]; 
			if (difAño>0) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Fallo en la URI, revise los parametros fechaIni y fechaFin. El año de Inicio no puede ser posterior al de fin.")
						.build();
			}else if(difAño<0) {
				secsFin[0]+=31540000;
			}			

			if (fechaIni != "0" && fechaFin != "0") {
				String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + ";";
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				String numeroPosts = "<?xml version=\"1.0\"?>" + "<numposts>";
				rs.beforeFirst();
				int cont = 0;
				while (rs.next()) {
					int fechaPostSecs[] = getFechaInSecs(postFromRS(rs).getFecha());
					difAño = secsIni[1]-fechaPostSecs[1];
					if(difAño<0) {
						secsFin[0]+=31540000;
					}
					if(fechaPostSecs[0]<= secsFin[0] && fechaPostSecs[0]>=secsIni[0]) {
						cont++;
					}
				}
				numeroPosts += cont + "</numposts>";
				return Response.status(Response.Status.OK).entity(numeroPosts).build();				
			}else {
				return Response.status(Response.Status.BAD_REQUEST).entity("Fallo en la URI, revise los parametros number, limIzq y limDer")
						.build();
			}
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity("No se pudieron convertir los índices a números")
					.build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	@GET
	@Path("bylimitdate/{id_Propietario}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getPostsByLimitDate(@PathParam("id_Propietario") String idPropietario,
			@QueryParam("fechaLimite") @DefaultValue("0") String fechaLimite,
			@QueryParam("numero") @DefaultValue("0") String numero,
			@QueryParam("limIzq") @DefaultValue("0") String limIzq, 
			@QueryParam("limDer") @DefaultValue("0") String limDer) throws ParseException {
		try {
			if (fechaLimite != "0") {
				int num = Integer.parseInt(numero);
				int limI = Integer.parseInt(limIzq);
				int limD = Integer.parseInt(limDer);
				if(limI != 0 && limD != 0 && num== 0) {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + ";";
					PreparedStatement ps;
					ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					String listado = "<?xml version=\"1.0\"?>" + "<posts>";
					rs.beforeFirst();
					int cont = 1;
					while (rs.next()) {
						if(cont >= limI && cont <= limD) {
							int fechaPost[] = getFechaInSecs(postFromRS(rs).getFecha());
							int fechaLimit[] = getFechaInSecs(fechaLimite);
							if (fechaPost[1]<fechaLimit[1] || fechaPost[1]==fechaLimit[1] && fechaPost[0]<=fechaLimit[0]) {
								listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" + rs.getInt("idPost") + "\" />";
							}
						}
						cont++;
					}
					listado = listado + "</posts>";
					return Response.status(Response.Status.OK).entity(listado).build(); 
				}else if (num!=0 && limI == 0 && limD == 0) {
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + " LIMIT " + numero + ";";
					PreparedStatement ps;
					ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					String listado = "<?xml version=\"1.0\"?>" + "<posts>";
					rs.beforeFirst();
					while (rs.next()) {
						int fechaPost[] = getFechaInSecs(postFromRS(rs).getFecha());
						int fechaLimit[] = getFechaInSecs(fechaLimite);
						if (fechaPost[1]<fechaLimit[1] || fechaPost[1]==fechaLimit[1] && fechaPost[0]<=fechaLimit[0]) {
							listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" + rs.getInt("idPost") + "\" />";
						}
					}
				listado = listado + "</posts>";
				return Response.status(Response.Status.OK).entity(listado).build(); 
				}else if(num==0 && limI == 0 && limD == 0){
					String sql = "SELECT * FROM SocialUPM.Post WHERE idPropietario = " + idPropietario + ";";
					PreparedStatement ps;
					ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					String listado = "<?xml version=\"1.0\"?>" + "<posts>";
					rs.beforeFirst();
					while (rs.next()) {
						int fechaPost[] = getFechaInSecs(postFromRS(rs).getFecha());
						int fechaLimit[] = getFechaInSecs(fechaLimite);
						if (fechaPost[1]<fechaLimit[1] || fechaPost[1]==fechaLimit[1] && fechaPost[0]<=fechaLimit[0]) {
							listado = listado + "<post href=\"http://localhost:8080/SocialUPM/api/posts/" + rs.getInt("idPost") + "\" />";
						}
						
					}
					listado = listado + "</posts>";
					return Response.status(Response.Status.OK).entity(listado).build();
				}else {
					return Response.status(Response.Status.BAD_REQUEST).entity("Fallo en la URI, revise los valores de los parametros, no debe haber incongruencia entre ellos.")
							.build();
				}
			}else {
				return Response.status(Response.Status.BAD_REQUEST).entity("Fallo en la URI, revise la fecha limite, debe tener un valor valido.")
						.build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error de acceso a BBDD").build();
		}
	}
	
	
	
	private int [] getFechaInSecs(String date) throws ParseException {
		String []dateParts = date.split(" ");
		String auxFecha = dateParts[0];
		String auxHora = dateParts[1];
		int fechaSecs[] = new int [2];
		String []parteFecha = auxFecha.split("-");
		int mesSecs = Integer.parseInt(parteFecha[1])*2628000;
		int diaSecs = Integer.parseInt(parteFecha[2])*86400;
		fechaSecs[0]+= mesSecs + diaSecs;
		String []parteHora = auxHora.split(":");
		if (parteHora[2].contains(".")) {
			double segs = Double.parseDouble(parteHora[2]);
			fechaSecs[0]+=Integer.parseInt(parteHora[0])*3600+Integer.parseInt(parteHora[1])*60+ (int)segs;
			
		}else {
			int horaSegs = Integer.parseInt(parteHora[0])*3600;
			int minSegs = Integer.parseInt(parteHora[1])*60;
			int segs = Integer.parseInt(parteHora[2]);
			fechaSecs[0]+=horaSegs +minSegs+segs;
		}
		fechaSecs[1] = Integer.parseInt(parteFecha[0]);
		return fechaSecs;
	}
	
	private Post postFromRS(ResultSet rs) throws SQLException {
		Post post = new Post(rs.getInt("idPost"), rs.getString("Message"), rs.getString("Date"), rs.getInt("idPropietario"));
		post.setId_Post(rs.getInt("idPost"));
		return post;
	}

	
	
	

}