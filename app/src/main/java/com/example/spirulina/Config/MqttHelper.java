package com.example.spirulina.Config;

public class MqttHelper {
//    public MqttAndroidClient mqttAndroidClient;
//
//    private final String serverUri = "tcp://m24.cloudmqtt.com:13699";
//    private final String clientId = "iot_user_test";
//    private final String subscriptionTopic = "actuator/+";
//
//    private final String username = "fhofapmr";
//    private final String password = "h5QtLdmK7z7B";
//
//    public MqttHelper(Context context){
//        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
//        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean b, String s) {
//                Log.w("mqtt", s);
//            }
//
//            @Override
//            public void connectionLost(Throwable throwable) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w("Mqtt", mqttMessage.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//            }
//        });
//        connect();
//    }
//
//    public void setCallback(MqttCallbackExtended callback) {
//        mqttAndroidClient.setCallback(callback);
//    }
//
//    private void connect(){
//        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        mqttConnectOptions.setAutomaticReconnect(true);
//        mqttConnectOptions.setCleanSession(false);
//        mqttConnectOptions.setConnectionTimeout(240000);
////        mqttConnectOptions.setUserName(username);
////        mqttConnectOptions.setPassword(password.toCharArray());
//
//        try {
//
//            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
////
////                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
////                    disconnectedBufferOptions.setBufferEnabled(true);
////                    disconnectedBufferOptions.setBufferSize(100);
////                    disconnectedBufferOptions.setPersistBuffer(false);
////                    disconnectedBufferOptions.setDeleteOldestMessages(false);
////                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
////                    subscribeToTopic();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
//                }
//            });
//
//
//        } catch (MqttException ex){
//            ex.printStackTrace();
//        }
//    }
//
//
//    private void subscribeToTopic() {
//        try {
//            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.w("Mqtt","Subscribed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w("Mqtt", "Subscribed fail!");
//                }
//            });
//
//        } catch (MqttException ex) {
//            System.err.println("Exceptionst subscribing");
//            ex.printStackTrace();
//        }
//    }
}
