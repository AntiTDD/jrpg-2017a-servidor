package comandos;

import mensajeria.Comando;
import servidor.EscuchaCliente;

public abstract class ComandosServer extends Comando{
	protected EscuchaCliente escuchaCliente;

	public void setEscuchaCliente(EscuchaCliente escuchaCliente) {
		this.escuchaCliente = escuchaCliente;
	}
	
}
