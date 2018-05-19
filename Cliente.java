package clase.cliente;
import clase.datos.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class Cliente {

	public static ArrayList<String> getUris(String str) {
		ArrayList<String> res = new ArrayList<String>();
		String div[] = str.split("http://localhost:8080/SocialUPM/api");
		for(int i = 1; i<div.length;i++) { //me salto el 1er elemento
			res.add(div[i].split("/>")[0].replace("\"", ""));
		}
		
		return res;
	}
	
	
    public static void main(String[] args) throws Exception{

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target(getBaseURI());
        
      //IMPORTANTE:Ir descomentando metodos segun se vayan a utilizar
        
      //-----------------------------------------------Crear un usuario------------------------------------------------------
              
//        Usuario user=new Usuario("Juan","España","Hola.");
//        Response response = target.path("/usuario").request().post(Entity.json(user));
//        System.out.println("Estado: "+response.getStatus());
//        response.close();

      //-----------------------------------------------Borrar un estado------------------------------------------------------
        
//      Response response = target.path("3")
//            .request()
//            .delete();  // Establece Content Types
//      response.close();
//      System.out.println("Estado: " + response.getStatus());
      
      
//-----------------------------------------------Buscar posibles amigos------------------------------------------------
//      
//      Response response = target.path("usuario/buscar")
//        	  .queryParam("cadena", "v")
//              .request()
//              .accept(MediaType.APPLICATION_JSON)
//              .get(Response.class);
//      
//      System.out.println("Estado: " + response.getStatus());
//      String valor = response.readEntity(String.class);
//      System.out.println("Entidad: " + valor);
      
      
//-----------------------------------------------Agregar un amigo------------------------------------------------------
      
//      Solicitud sol=new Solicitud(10,8);
//      Response response = target.path("/solicitud").request().post(Entity.json(sol));
//      System.out.println("Estado: "+response.getStatus());
//      System.out.println(response.readEntity(String.class));
//      response.close();
      
      
//-----------------------------------------------Eliminar un amigo-----------------------------------------------------
      
//      Response response = target.path("/solicitud/14")
//      		.request()
//      		.delete();  // Establece Content Types
//      response.close();
//      System.out.println("Estado: " + response.getStatus());
      
      
//-----------------------------------------------Obtener lista de amigos usando los filtros----------------------------       
      
      
      //lista que selecciona todos los amigos
//      Response res = target.path("/usuario/listaAmigos/1")
//      		.request()
//      		.accept(MediaType.APPLICATION_JSON)
//      		.get(Response.class);
//
//      System.out.println("Estado: " + res.getStatus());
//      String valor = res.readEntity(String.class);
//      System.out.println("Entidad: " + valor);
      
      //lista que seleccina todos los amigos con un determinado patron en su nombre
//      Response res = target.path("/usuario/listaAmigos/10")
//      		.queryParam("nombre", "al")
//      		.request()
//      		.accept(MediaType.APPLICATION_JSON)
//      		.get(Response.class);
//
//      System.out.println("Estado: " + res.getStatus());
//      String valor = res.readEntity(String.class);
//      System.out.println("Entidad: " + valor);
      
      //lista que selecciona todos los amigos en un intervalo
//      Response res = target.path("/usuario/listaAmigos/10")
//      		.queryParam("limInf", "2")
//      		.queryParam("limSup", "3")
//      		.request()
//      		.accept(MediaType.APPLICATION_JSON)
//      		.get(Response.class);
//
//      System.out.println("Estado: " + res.getStatus());
//      String valor = res.readEntity(String.class);
//      System.out.println("Entidad: " + valor);
      
      //lista que selecciona los n primeros amigos
//      Response res = target.path("/usuario/listaAmigos/10")
//      		.queryParam("num", "1")
//      		.request()
//      		.accept(MediaType.APPLICATION_JSON)
//      		.get(Response.class);
//
//      System.out.println("Estado: " + res.getStatus());
//      String valor = res.readEntity(String.class);
//      System.out.println("Entidad: " + valor);
      
        //Envío de un post
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String fechaActual = now.format(formatter);
//        Post post = new Post(0,"Prueba Cliente Java",fechaActual,1);//id del propietario falta
//        Response response = target
//                .request()
//                .post(Entity.json(post));  // Establece Content Types
//        System.out.println("Estado: " + response.getStatus());
//        response.close();
//        

        //Obtener mis estados id
//        
//        Response response= target.path("posts/getpostsid/" + 1)
//                .request()
//                .accept(MediaType.APPLICATION_XML)
//                .get(Response.class);
//        System.out.println("Estado: " + response.getStatus());
//        String valor = response.readEntity(String.class);
//        System.out.println("Entidad: " + valor);
//        response.close();
//        
//        //Obtener mis estados por contenido
//        ArrayList <String> urisPosts = getUris(valor);
//        System.out.println("Entidad:");
//        for (int i = 0; i<urisPosts.size();i++) {
//        	String path = urisPosts.get(i);
//        	
//	        Response res = (target.path(path.substring(1, path.length()-1))
//	                .request()
//	                .accept(MediaType.APPLICATION_XML)
//	                .get(Response.class));
//	        System.out.println("Estado: " + res.getStatus());
//	        System.out.println(res.readEntity(Post.class).getContenido());
//	        res.close();
//        }

        //Borrar un estado
//       Response response = target.path("1")
//              .request()
//              .delete();  // Establece Content Types
//      response.close();
//      System.out.println("Estado: " + response.getStatus());
        
      //Consultar numero estados publicados por mi en un periodo
//        Response response= target.path("posts/count/" + 1)
//        	  .queryParam("fechaIni", "2018-05-17 01:02:00")
//        	  .queryParam("fechaFin", "2018-05-17 11:10:35")
//              .request()
//              .accept(MediaType.APPLICATION_XML)
//              .get(Response.class);
//        System.out.println("Estado: " + response.getStatus());
//        String valor = response.readEntity(String.class);
//        System.out.println("Entidad: " + valor);
//        
        //Obtener lista de los ultimos estados de nuestros amigos
     
//        Response response= target.path("posts/byFriendsAndDate/" + 1)
//        		.queryParam("fechaLimite", "2018-05-17 19:00:00")
//        		.queryParam("numero", "3")
//        		.request()
//        		.accept(MediaType.APPLICATION_XML)
//        		.get(Response.class);
//        
//        System.out.println("Estado: " + response.getStatus());
//        String valor = response.readEntity(String.class);
//        System.out.println("Entidad: " + valor);
        
        //Obtener lista de posts de mis amigos con determinado texto
        
//        Response response= target.path("posts/bycontent/" + 1)
//        		.queryParam("contenido", "Soy Usuario")
//				.request()
//				.accept(MediaType.APPLICATION_XML)
//				.get(Response.class);
//		
//		System.out.println("Estado: " + response.getStatus());
//		String valor = response.readEntity(String.class);
//		System.out.println("Entidad: " + valor);
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/SocialUPM/api").build();
    }
    
}
