import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

//*********************************************************************************************************************
//                                                                                                                    *
// Autor: Antonio Bernardo de Vasconcellos Praxedes                                                                   *
//                                                                                                                    *  
// Data: 02/09/2021                                                                                                   *
//                                                                                                                    *
// Nome da Classe: HTTPSrvCloud                                                                                       *
//                                                                                                                    *
// Funcao: Programa Principal Servidor HTTP - Operação na Intranet ou na Nuvem definida na configuração               *
//                                                                                                                    *
//*********************************************************************************************************************
//
public class ServHTTPMain implements Runnable {
	
	private static boolean Verbose = true;
	private static String MsgXML = "";
	private static int Contador = 0;
	private static String Caminho = "";
	private static String CaminhoNuvem = "/home/bernardo/Executavel/";
	private static String CaminhoLocal = "/home/antonio/Workspace/Recursos/";
	
	private Socket connect;
				
	public ServHTTPMain(Socket c) {
		connect = c;
	}

	//*****************************************************************************************************************
	//                                                                                                                *
    // Método Executavel da ClasseHTTPSrvSup                                                                          *
	//                                                                                                                *
	// Funcao: Servidor HTTP aguarda a conexão do Cliente                                                             *
	//                                                                                                                *
	//*****************************************************************************************************************
	//
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int Porta = 8080;
		
		try {
			ServerSocket serverConnect = new ServerSocket(Porta);
			InetAddress ip = InetAddress.getLocalHost();
			String NomeComputador = "";
			NomeComputador = ip.getHostName();
			
			if (NomeComputador.equals("antonio-Vostro1510")) {
				Caminho = CaminhoLocal;
				Util.Terminal("Servidor Iniciado no Computador" + NomeComputador, false, true);
			}
			else {
				Caminho = CaminhoNuvem;
				Util.Terminal("Servidor Iniciado no Computador na Nuvem" + Porta, false, true);
			}
			
			Util.Terminal("Esperando por Conexoes na Porta: " + Porta, false, Verbose);
			
			while (true) {    // Espera a conexão do cliente
				ServHTTPMain myServer = new ServHTTPMain(serverConnect.accept());
				Util.Terminal("Conexao Aberta com o Cliente (" + new Date() + ")", false, Verbose);
				Thread thread = new Thread(myServer);      // Thread para gerenciar a conexão do cliente
				thread.start();
			}
		} catch (IOException e) {
			System.err.println("Erro na Conexao com o Servidor: " + e.getMessage());
		}
	} // Fim da Rotina public static void main(String[] args) {
	
	
	//***************************************************************************************************************************
	//                                                                                                                          *
    // Processa a Solicitação do Cliente                                                                                        *
	//                                                                                                                          *
	// Funcao: processa a solicitação do Cliente HTTP                                                                           *
	//                                                                                                                          *
	//***************************************************************************************************************************
	//
	//@Override
	public void run() {
		
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		InputStreamReader dataIn = null; InputStream ByteIn = null; //String fileRequested = null;
						
		try {
			ByteIn = connect.getInputStream();
			dataIn = new InputStreamReader(ByteIn); //
			in = new BufferedReader(dataIn);  //new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			String LinhaCab[] = new String[12];
			int CChar = 0;
			int CLin = 0;
			int ChRec = 0;
			int CR = 13;
			int LF = 10;
			boolean Leu_CRLF = false;
			boolean fim = false;
			String Requisicao = null;
			LinhaCab[0] = "";
			boolean mobile = false;
									
			while (!fim) {
				ChRec = ByteIn.read();
				CChar = CChar + 1;
				if (CChar > 2000) { fim = true; }
				if (ChRec == CR) {
					ChRec = ByteIn.read();
					if (ChRec == LF) {
						if (Leu_CRLF) {
							fim = true;
						}
						else {
							LinhaCab[CLin] = LinhaCab[CLin] + "\n";
							CLin = CLin + 1;
							LinhaCab[CLin] = "";
							Leu_CRLF = true;
						}
					}
				}
				else {
					LinhaCab[CLin] = LinhaCab[CLin] + (char)ChRec;
					Leu_CRLF = false;
				}
			}	
					
			// Monta o Cabeçalho da Requisição na String CabHTTP
			String CabHTTP = "";
			for (int k = 0; k < CLin; k++){
				CabHTTP = CabHTTP + LinhaCab[k] + "\n";
				//System.out.println("k = " + k + " - Linha: " + LinhaCab[k]);
			}
			
			if (CabHTTP.toLowerCase().indexOf("mobile") >= 0) {
				mobile = true;
				System.out.println("Acesso por Dispositivo Móvel");
				Util.Terminal("Acesso por Dispositivo Móvel", false, Verbose);
			}
			else {
				mobile = false;
			}
			
			StringTokenizer parseLinha1 = new StringTokenizer(LinhaCab[0]);
			String method = parseLinha1.nextToken().toUpperCase();
			String ArquivoReq = "";
			
			if (parseLinha1.hasMoreTokens()) {;
				Requisicao = parseLinha1.nextToken();
				ArquivoReq = Requisicao.substring(1);
			}
			
			int TamArqReq = ArquivoReq.length();
			String ArqReq = "";
			
			boolean RecMetodoValido = false;
			boolean RecReqValida = false;
			Util.Terminal("Método: " + method + "  -  Arquivo Requisitado: " + ArquivoReq, false, Verbose);
			
			Mensagem Msg = new Mensagem();
			
			if (method.equals("GET")) {  // Trata o método GET
				RecMetodoValido = true;
				
				// Se não há requisição de arquivo, solicita arquivo index.html (página raiz)
				if (Requisicao.equals("/") || Requisicao.equals("/?")) {
					RecReqValida = EnvRecMsg.EnvArqTxt(connect, Caminho, "index.html", Verbose);
				}
				else { // Trata a requisição do método GET
					
					// Trata requisições de arquivos texto de página HTML
					if (ArquivoReq.endsWith(".html")) {
						
						if (mobile) {
							ArqReq = ArquivoReq.substring(0, TamArqReq - 5);
							ArqReq = ArqReq + ".m.html"; 
							}
						else {
							ArqReq = ArquivoReq;
						}
						RecReqValida = EnvRecMsg.EnvArqTxt(connect, Caminho, ArqReq, Verbose);
					}
					
					// Trata requisições de arquivos texto de estilos (CSS)
					if (ArquivoReq.endsWith(".css")) {
						if (mobile) {
							ArqReq = ArquivoReq.substring(0, TamArqReq - 4);
							ArqReq = ArqReq + ".m.css"; 
							}
						else {
							ArqReq = ArquivoReq;
						}
						RecReqValida = EnvRecMsg.EnvArqTxt(connect, Caminho, ArqReq, Verbose);
					}
					
					// Trata requisições de arquivos de programas Javascript
					if (ArquivoReq.endsWith(".js")) {
						if (mobile) {
							ArqReq = ArquivoReq.substring(0, TamArqReq - 3);
							ArqReq = ArqReq + ".m.js"; 
							}
						else {
							ArqReq = ArquivoReq;
						}
						RecReqValida = EnvRecMsg.EnvArqTxt(connect, Caminho, ArqReq, Verbose);
					}
					
					// Trata requisições de arquivos de imagem
					if (ArquivoReq.endsWith(".ico") || ArquivoReq.endsWith(".jpg") || ArquivoReq.endsWith(".png")) {
						RecReqValida = EnvRecMsg.EnvArqByte(connect, Caminho, ArquivoReq, Verbose);
					}
						
					// Trata requisição de mensagem XML de Atualização dos Valores das Variáveis
					if (ArquivoReq.endsWith("local001.xml")) {
						RecReqValida = true;
						Contador = Contador + 1;
						System.out.println("Contador = " + Contador);
						if (Contador < 8) {
							EnvRecMsg.EnvString(connect, MsgXML, "text/xml", "200", Verbose);
						}
						else {
							EnvRecMsg.EnvString(connect, Msg.MontaXMLFalha(0), "text/xml", "200", Verbose);
						}
					}
				} // else if (Requisicao.equals("/") || Requisicao.equals("/?")) {
			}  // if (method.equals("GET"))
							    
			if (method.equals("POST")) {              // Se método = POST,
				RecMetodoValido = true;
				if (ArquivoReq.equals("atualiza")) {  // e requisição = "atualiza", indica mensagem binária de atualização
					RecReqValida = true;
					
					String TamMsg = "";       // TamMsg = string com o número de caracteres/bytes da mensagem
					String TipoMsg = "";      // TipoMsg = string com o tipo da mensagem (XML ou octet/stream"
					int TamanhoMsg = 0;       // TamanhoMensagem = inteiro com o número de caracteres/bytes da mensagem
					StringTokenizer parseLinha3 = new StringTokenizer(LinhaCab[2]); // Linha 3
					String IdLinha3 =  parseLinha3.nextToken().toLowerCase();       // IdLinha3 minúsculo deve ser "Content-Length:"
					StringTokenizer parseLinha4 = new StringTokenizer(LinhaCab[3]); // Linha 4
					String IdLinha4 = parseLinha4.nextToken().toLowerCase();        // IdLinha4 minúsculo deve ser "Content-Type:"
					
					if (IdLinha3.equals("content-length:") && IdLinha4.equals("content-type:")) {
						TamMsg = parseLinha3.nextToken();                  
						TamanhoMsg = Util.StringToInt(TamMsg);
						TipoMsg = parseLinha4.nextToken().toLowerCase();
												
						if (TipoMsg.equals("application/octet-stream")) {  // Se é mensagem do tipo binária
							
							int[] MsgBin = new int[512];
							for (int i = 0; i < TamanhoMsg; i++){
								MsgBin[i] = ByteIn.read();           // Recebe os bytes e carrega no buffer
							}
							
							if ((MsgBin[0] == 0x60) && (MsgBin[1] == 0x45)) {  // Se recebeu mensagem CoAP válida,
								Msg.CarregaVariaveis(MsgBin);
								
								if (Msg.getEstCom1() == 1) {                   // Se a comunicacao com o programa de atualização está OK,
									MsgXML = Msg.MontaXML();                   // monta a mensagem XML para enviar ao Navegador
								}
								else {                        		    // Se a comunicacao com o Atualizador está em falha,
									Msg.MontaXMLFalha(1);     		    // monta a mensagem XML de falha para enviar ao Navegador
								}
								Util.Terminal("Recebida Mensagem Binária de Atualizacao com " + TamanhoMsg + " Bytes", false, Verbose);
								
								// Responde com mensagem de XML de comando
								String StrComando = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
								StrComando = StrComando + "<CMD></CMD>";
								//EnvMsgStringTxt(StrComando, "text/xml", "200");
								EnvRecMsg.EnvString(connect, StrComando, "text/xml", "200", Verbose);
								Contador = 0;
							}
							else {
								Util.Terminal("Recebida Mensagem de Atualizacao Invalida", false, Verbose);
								String StrComando = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
								StrComando = StrComando + "<CMD>MsgInv</CMD>";
								EnvRecMsg.EnvString(connect, StrComando, "text/xml", "200", Verbose);
							}
						} // if (TipoMsg.equals("application/octet-stream"))
					} // if ((IdLinha3 == "content-length:") && (IdLinha4 == "content-type:"))
				}  // if (Requisicao.equals("atualiza"))
			} // if (method.equals("POST"))
			
			if (RecMetodoValido) {    // Se foi recebido um método válido,
				if (!RecReqValida) {  // e se não está disponível o recurso solicitado pelo método GET ou POST
					EnvRecMsg.EnvStringErro(connect, 404, Verbose);
				}
			}
			else {                    // Se não foi recebido um método válido,
				EnvRecMsg.EnvStringErro(connect, 501, Verbose);
			}
			
		/*} catch (FileNotFoundException fnfe) {
			try {
				Util.Terminal("Arquivo não encontrado", false, Verbose);
			} catch (IOException ioe) {
				Util.Terminal("Erro na conexão: " + ioe.getMessage(), false, Verbose);
			}*/
			
		} catch (IOException ioe) {
			Util.Terminal("Erro no Servidor: " + ioe, false, Verbose);
		} finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close();
			} catch (Exception e) {
				Util.Terminal("Erro no fechamento do stream : " + e.getMessage(), false, Verbose);
			} 
			Util.Terminal("Conexao com o Cliente Encerrada", false, Verbose);
			
		}
	}
}

