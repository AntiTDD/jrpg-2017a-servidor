package comandos;

import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import servidor.Servidor;

public class Conexion extends ComandosServer {

	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) (gson.fromJson(cadenaLeida, PaquetePersonaje.class)).clone());

		Servidor.getPersonajesConectados().put(escuchaCliente.getPaquetePersonaje().getId(), (PaquetePersonaje) escuchaCliente.getPaquetePersonaje().clone());
		Servidor.getUbicacionPersonajes().put(escuchaCliente.getPaquetePersonaje().getId(), (PaqueteMovimiento) new PaqueteMovimiento(escuchaCliente.getPaquetePersonaje().getId()).clone());
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
