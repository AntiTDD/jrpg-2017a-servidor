package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import dominio.Item;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

public class Conector {

	private String url = "primeraBase.bd";
	static Connection connect;
	private static final int CANTIDADITEMS = 60;

	public void connect() {
		try {
			Servidor.log.append("Estableciendo conexi�n con la base de datos..." + System.lineSeparator());
			connect = DriverManager.getConnection("jdbc:sqlite:" + url);
			Servidor.log.append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	public void close() {
		try {
			connect.close();
		} catch (SQLException ex) {
			Servidor.log.append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
			Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean registrarUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			PreparedStatement st1 = connect.prepareStatement("SELECT * FROM registro WHERE usuario= ? ");
			st1.setString(1, user.getUsername());
			result = st1.executeQuery();

			if (!result.next()) {

				PreparedStatement st = connect.prepareStatement("INSERT INTO registro (usuario, password, idPersonaje) VALUES (?,?,?)");
				st.setString(1, user.getUsername());
				st.setString(2, user.getPassword());
				st.setInt(3, user.getIdPj());
				st.execute();
				Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
				return true;
			} else {
				Servidor.log.append("El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
				return false;
			}
		} catch (SQLException ex) {
			Servidor.log.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(ex.getMessage());
			return false;
		}

	}

	public boolean registrarPersonaje(PaquetePersonaje paquetePersonaje, PaqueteUsuario paqueteUsuario) {

		try {

			// Registro al personaje en la base de datos
			PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
					"INSERT INTO personaje (idInventario, idMochila,casta,raza,fuerza,destreza,inteligencia,saludTope,energiaTope,nombre,experiencia,nivel,idAlianza) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			stRegistrarPersonaje.setInt(1, -1);
			stRegistrarPersonaje.setInt(2, -1);
			stRegistrarPersonaje.setString(3, paquetePersonaje.getCasta());
			stRegistrarPersonaje.setString(4, paquetePersonaje.getRaza());
			stRegistrarPersonaje.setInt(5, paquetePersonaje.getFuerza());
			stRegistrarPersonaje.setInt(6, paquetePersonaje.getDestreza());
			stRegistrarPersonaje.setInt(7, paquetePersonaje.getInteligencia());
			stRegistrarPersonaje.setInt(8, paquetePersonaje.getSaludTope());
			stRegistrarPersonaje.setInt(9, paquetePersonaje.getEnergiaTope());
			stRegistrarPersonaje.setString(10, paquetePersonaje.getNombre());
			stRegistrarPersonaje.setInt(11, 0);
			stRegistrarPersonaje.setInt(12, 1);
			stRegistrarPersonaje.setInt(13, -1);
			stRegistrarPersonaje.execute();

			// Recupero la �ltima key generada
			ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();
			if (rs != null && rs.next()) {
				// Obtengo el id
				int idPersonaje = rs.getInt(1);

				// Le asigno el id al paquete personaje que voy a devolver
				paquetePersonaje.setId(idPersonaje);

				// Le asigno el personaje al usuario
				PreparedStatement stAsignarPersonaje = connect.prepareStatement("UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
				stAsignarPersonaje.setInt(1, idPersonaje);
				stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
				stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
				stAsignarPersonaje.execute();

				// Por ultimo registro el inventario y la mochila
				if (this.registrarInventarioMochila(idPersonaje)) {
					Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
							+ paquetePersonaje.getId() + System.lineSeparator());
					return true;
				} else {
					Servidor.log.append("Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername() + " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
					return false;
				}
			}
			return false;

		} catch (SQLException e) {
			Servidor.log.append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			e.printStackTrace();
			return false;
		}

	}

	public boolean registrarInventarioMochila(int idInventarioMochila) {
		try {
			// Preparo la consulta para el registro del inventario en la base de
			// datos
			PreparedStatement stRegistrarInventario = connect.prepareStatement("INSERT INTO inventario(idInventario,manos1,manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
			stRegistrarInventario.setInt(1, idInventarioMochila);

			// Preparo la consulta para el registro la mochila en la base de
			// datos
			PreparedStatement stRegistrarMochila = connect.prepareStatement("INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,item16,item17,item18,item19,item20) VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
			stRegistrarMochila.setInt(1, idInventarioMochila);

			// Registro inventario y mochila
			stRegistrarInventario.execute();
			stRegistrarMochila.execute();

			// Le asigno el inventario y la mochila al personaje
			PreparedStatement stAsignarPersonaje = connect
					.prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
			stAsignarPersonaje.setInt(1, idInventarioMochila);
			stAsignarPersonaje.setInt(2, idInventarioMochila);
			stAsignarPersonaje.setInt(3, idInventarioMochila);
			stAsignarPersonaje.execute();

			Servidor.log.append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
			return true;

		} catch (SQLException e) {
			Servidor.log.append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
			e.printStackTrace();
			return false;
		}
	}

	public boolean loguearUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Busco usuario y contrase�a
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
			st.setString(1, user.getUsername());
			st.setString(2, user.getPassword());
			result = st.executeQuery();

			// Si existe inicio sesion
			if (result.next()) {
				Servidor.log.append("El usuario " + user.getUsername() + " ha iniciado sesi�n." + System.lineSeparator());
				return true;
			}

			// Si no existe informo y devuelvo false
			Servidor.log.append("El usuario " + user.getUsername() + " ha realizado un intento fallido de inicio de sesi�n." + System.lineSeparator());
			return false;

		} catch (SQLException e) {
			Servidor.log.append("El usuario " + user.getUsername() + " fallo al iniciar sesi�n." + System.lineSeparator());
			e.printStackTrace();
			return false;
		}

	}

	public void actualizarPersonaje(PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect
					.prepareStatement("UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=? "
							+ "  WHERE idPersonaje=?");
		
			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());
			
			stActualizarPersonaje.executeUpdate();
			this.actualizarInventario(paquetePersonaje);
					
			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con �xito."  + System.lineSeparator());
			
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()  + System.lineSeparator());
			e.printStackTrace();
		}
		
		
	}

	public PaquetePersonaje getPersonaje(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Selecciono el personaje de ese usuario
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, user.getUsername());
			result = st.executeQuery();

			// Obtengo el id
			int idPersonaje = result.getInt("idPersonaje");

			// Selecciono los datos del personaje
			PreparedStatement stSeleccionarPersonaje = connect.prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
			stSeleccionarPersonaje.setInt(1, idPersonaje);
			result = stSeleccionarPersonaje.executeQuery();

			// Obtengo los atributos del personaje
			PaquetePersonaje personaje = new PaquetePersonaje();
			personaje.setId(idPersonaje);
			personaje.setRaza(result.getString("raza"));
			personaje.setCasta(result.getString("casta"));
			personaje.setFuerza(result.getInt("fuerza"));
			personaje.setInteligencia(result.getInt("inteligencia"));
			personaje.setDestreza(result.getInt("destreza"));
			personaje.setEnergiaTope(result.getInt("energiaTope"));
			personaje.setSaludTope(result.getInt("saludTope"));
			personaje.setNombre(result.getString("nombre"));
			personaje.setExperiencia(result.getInt("experiencia"));
			personaje.setNivel(result.getInt("nivel"));
			
			//Cargo el array list de Items del personaje
			cargarInventario(personaje);			

			// Devuelvo el paquete personaje con sus datos
			return personaje;

		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return new PaquetePersonaje();
	}
	
	public void cargarInventario (PaquetePersonaje personaje) {
		ResultSet result = null;
		ResultSet resultItem = null; 
		PreparedStatement stItems;
		PreparedStatement stDatosItem; //Con esta variable hago la consulta a la bd para saber los bonus de cada item en la mochila
		HashMap<String,Integer> bonus = new HashMap<String,Integer>();
		
		try {
			stItems = connect.prepareStatement("SELECT * FROM mochila WHERE idMochila = " + personaje.getId());
			result = stItems.executeQuery();
			
			
				for(int i=1; i<21; i++) {
					int idItem;
					idItem = result.getInt("item"+i);
					if (idItem != -1) {
						stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem ="+ idItem);
						resultItem = stDatosItem.executeQuery();
						bonus.put("bonoAtaque",resultItem.getInt("bonoAtaque"));
						bonus.put("bonoDefensa", resultItem.getInt("bonoDefensa"));
						bonus.put("BonoMagia", resultItem.getInt("BonoMagia"));
						bonus.put("bonoSalud", resultItem.getInt("bonoSalud"));
						bonus.put("bonoEnergia", resultItem.getInt("bonoEnergia"));
							
						Item itemMochila = new Item (resultItem.getInt("idItem"),bonus,new Integer (1));
						personaje.aniadirItem(itemMochila);
					}
					
					}
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void actualizarInventario(PaquetePersonaje paquetePersonaje) {
		
		int [] idItemsAct = new int [20];
		int i=0;
		try {
			PreparedStatement stActualizarInventario = connect.prepareStatement("UPDATE mochila SET item1=?, item2=?, item3=?, item4=?, item5=?, item6=?, item7=?, item8=?, item9=?, item10=?,item11=?, item12=?, item13=?, item14=?, item15=?, item16=?, item17=?, item18=?, item19=?, item20=? "+ "  WHERE idMochila=?");
			while( i < paquetePersonaje.getInventario().size() ){
				idItemsAct[i] = paquetePersonaje.getInventario().get(i).getId();
				i++;
			}
			if(i<19) {
				for(i=i; i<20; i++) {
					idItemsAct[i] = -1;
				}
			}
			stActualizarInventario.setInt(1, idItemsAct[0]);
			stActualizarInventario.setInt(2, idItemsAct[1]);
			stActualizarInventario.setInt(3, idItemsAct[2]);
			stActualizarInventario.setInt(4, idItemsAct[3]);
			stActualizarInventario.setInt(5, idItemsAct[4]);
			stActualizarInventario.setInt(6, idItemsAct[5]);
			stActualizarInventario.setInt(7, idItemsAct[6]);
			stActualizarInventario.setInt(8, idItemsAct[7]);
			stActualizarInventario.setInt(9, idItemsAct[8]);
			stActualizarInventario.setInt(10, idItemsAct[9]);
			stActualizarInventario.setInt(11, idItemsAct[10]);
			stActualizarInventario.setInt(12, idItemsAct[11]);
			stActualizarInventario.setInt(13, idItemsAct[12]);
			stActualizarInventario.setInt(14, idItemsAct[13]);
			stActualizarInventario.setInt(15, idItemsAct[14]);
			stActualizarInventario.setInt(16, idItemsAct[15]);
			stActualizarInventario.setInt(17, idItemsAct[16]);
			stActualizarInventario.setInt(18, idItemsAct[17]);
			stActualizarInventario.setInt(19, idItemsAct[18]);
			stActualizarInventario.setInt(20, idItemsAct[19]);
			stActualizarInventario.setInt(21, paquetePersonaje.getId());
			
			stActualizarInventario.executeUpdate();			
			Servidor.log.append("El inventario de " + paquetePersonaje.getNombre() + " se ha actualizado con �xito."  + System.lineSeparator());;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el inventario de " + paquetePersonaje.getNombre()  + System.lineSeparator());
			e.printStackTrace();
		}
		
	}
	
	public static Item darItemRand() {
		Item itemNuevo = new Item();
		Random idItem = new Random();
		HashMap<String,Integer> bonus = new HashMap<String,Integer>();
		
		ResultSet resultItem = null;
		PreparedStatement stItem;	
		try {
			stItem = connect.prepareStatement("SELECT * FROM item WHERE idItem ="+idItem.nextInt(CANTIDADITEMS));
			resultItem = stItem.executeQuery();
			itemNuevo.setId(resultItem.getInt("idItem"));
			bonus.put("bonoAtaque",resultItem.getInt("bonoAtaque"));
			bonus.put("bonoDefensa", resultItem.getInt("bonoDefensa"));
			bonus.put("BonoMagia", resultItem.getInt("BonoMagia"));
			bonus.put("bonoSalud", resultItem.getInt("bonoSalud"));
			bonus.put("bonoEnergia", resultItem.getInt("bonoEnergia"));
			itemNuevo.setBonus(bonus);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return itemNuevo;
	}
	
	public PaqueteUsuario getUsuario(String usuario) {
		ResultSet result = null;
		PreparedStatement st;
		
		try {
			st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, usuario);
			result = st.executeQuery();

			String password = result.getString("password");
			int idPersonaje = result.getInt("idPersonaje");
			
			PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
			paqueteUsuario.setUsername(usuario);
			paqueteUsuario.setPassword(password);
			paqueteUsuario.setIdPj(idPersonaje);
			
			return paqueteUsuario;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
			Servidor.log.append(e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		}
		
		return new PaqueteUsuario();
	}
}