package comandos;


import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;
public class CreacionPJ extends ComandosServer {

	@Override
	public void ejecutar() {
		// Casteo el paquete personaje
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) (gson.fromJson(cadenaLeida, PaquetePersonaje.class)));
		
		// Guardo el personaje en ese usuario
	Servidor.getConector().registrarPersonaje(escuchaCliente.getPaquetePersonaje(), escuchaCliente.getPaqueteUsuario());
		
		// Le envio el id del personaje
		try {
			escuchaCliente.getSalida().writeObject(gson.toJson(escuchaCliente.getPaquetePersonaje(), escuchaCliente.getPaquetePersonaje().getClass()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
