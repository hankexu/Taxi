import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by hanke on 2016-01-07.
 */
public class TaxiServer {

    public static void main(String[] args){
        NioSocketAcceptor acceptor = new NioSocketAcceptor();

        acceptor.getFilterChain().addLast("LineCodec",new ProtocolCodecFilter(new TextLineCodecFactory()));
        acceptor.setHandler(new MessageHandler());

        try {
            acceptor.bind(new InetSocketAddress(9999));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
