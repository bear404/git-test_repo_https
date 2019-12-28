
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class app{
    public static void main(String[] args) throws Exception{
        System.out.println("counter app");

        //创建User实例
        String keyFile = "../solo-network/msp/keystore/user-key.pem";
        String certFile = "../solo-network/msp/signcerts/user-cert.pem";
        LocalUser user = new LocalUser("admin","SampleOrg",keyFile,certFile);

        //创建HFClient实例
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user);

        //创建通道实例
        Channel channel = client.newChannel("ch1");
        Peer peer = client.newPeer("peer1`","grpc://127.0.0.1:7051");
        channel.addPeer(peer);
        Orderer orderer = client.newOrderer("orderer1","grpc://127.0.0.1:7050");
        channel.addOrderer(orderer);
        channel.initialize();

        //查询链码
        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("counter-cc").build();
        req.setChaincodeID(cid);
        req.setFcn("value");
        ProposalResponse[] rsp = channel.queryByChaincode(req).toArray(new ProposalResponse[0]);
        System.out.format("rsp message => %s\n",rsp[0].getProposalResponse().getResponse().getPayload().toStringUtf8());

        //提交链码交易
        TransactionProposalRequest req2 = client.newTransactionProposalRequest();
        req2.setChaincodeID(cid);
        req2.setFcn("inc");
        req2.setArgs("10");
        Collection<ProposalResponse> rsp2 = channel.sendTransactionProposal(req2);
        TransactionEvent event = channel.sendTransaction(rsp2).get();
        System.out.format("txid: %s\n", event.getTransactionID());
        System.out.format("valid: %b\n", event.isValid());
    }
}