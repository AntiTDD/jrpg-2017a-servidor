package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteFinalizarBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class FinalizarBatalla extends ComandosServer {

	@Override
	public void ejecutar() {
		
		escuchaCliente.setPaqueteFinalizarBatalla((PaqueteFinalizarBatalla) gson.fromJson(cadenaLeida, PaqueteFinalizarBatalla.class));
		
		Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteFinalizarBatalla().getId()).setEstado(Estado.estadoJuego);
		Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo()).setEstado(Estado.estadoJuego);
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getIdPersonaje() == escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteFinalizarBatalla()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
