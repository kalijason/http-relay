package georgovassilis.httprelay.out;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parses command line arguments and constructs a {@link PrivateRelayProxy} instance
 * @author george georgovassilis
 *
 */
public class PrivateRelayProxyFactory {

	public final static String OPT_PROXY_TO_WEBSERVER = "proxy-to-webserver";
	public final static String OPT_PROXY_TO_RELAY = "proxy-to-relay";

	protected Logger log = LogManager.getLogger(getClass().getName());

	protected Proxy createProxy(String prescription) {

		Proxy proxy = Proxy.NO_PROXY;
		if (prescription != null) {
			Pattern p = Pattern.compile("(.*)://(.*):(\\d+)");
			Matcher m = p.matcher(prescription);
			if (m.matches()) {
				Proxy.Type type = Proxy.Type.valueOf(m.group(1).toUpperCase());
				InetSocketAddress addr = new InetSocketAddress(m.group(2), Integer.parseInt(m.group(3)));
				proxy = new Proxy(type, addr);
			} else
				throw new IllegalArgumentException(prescription + " doesn't match pattern protocol://address:port");
		}
		return proxy;
	}

	public PrivateRelayProxy createServerProxy(String args[]) throws Exception {
		Options options = new Options();
		options.addOption("bp", OPT_PROXY_TO_WEBSERVER, true,
				"If specified then a proxy will be used to contact the webserver. Format is protocol://address:port, e.g. http://proxy.example.com:8080 or socks://proxy.example.com:5000");
		options.addOption("rp", OPT_PROXY_TO_RELAY, true,
				"If specified then a proxy will be used to contact the public relay. Format is protocol://address:port, e.g. http://proxy.example.com:8080 or socks://proxy.example.com:5000");
		CommandLine cmd = new DefaultParser().parse(options, args);

		Proxy backendProxy = createProxy(cmd.getOptionValue(OPT_PROXY_TO_WEBSERVER));
		Proxy relayProxy = createProxy(cmd.getOptionValue(OPT_PROXY_TO_RELAY));
		if (backendProxy != Proxy.NO_PROXY)
			log.info("Using proxy " + backendProxy + " for talking to webserver");
		if (relayProxy != Proxy.NO_PROXY)
			log.info("Using proxy " + relayProxy + " for talking to public relay");

		PrivateRelayProxy privateRelayProxy = new PrivateRelayProxy(backendProxy, relayProxy);
		return privateRelayProxy;
	}

}