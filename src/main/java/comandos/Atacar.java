package comandos;

import java.io.IOException;

import mensajeria.PaqueteAtacar;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Atacar extends ComandosServer {
	@Override
	public void ejecutar() {
		escuchaCliente.setPaqueteAtacar((PaqueteAtacar) gson.fromJson(cadenaLeida, PaqueteAtacar.class));
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			if(conectado.getIdPersonaje() == escuchaCliente.getPaqueteAtacar().getIdEnemigo()) {
				try {
					conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteAtacar()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
