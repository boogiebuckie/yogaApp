import { initializeApp, getApps } from 'firebase/app';
import { getDatabase, ref, onValue, set, push } from 'firebase/database';

const firebaseConfig = {
  apiKey: "AIzaSyCC6zLZdogFlDLsX1357fQTUzCLyi9MrjA",
  authDomain: "comp1786-yogaapp-483b8.firebaseapp.com",
  databaseURL: "https://comp1786-yogaapp-483b8-default-rtdb.firebaseio.com",
  projectId: "comp1786-yogaapp-483b8",
  storageBucket: "comp1786-yogaapp-483b8.firebasestorage.app",
  messagingSenderId: "409521349738",
  appId: "1:409521349738:android:04f6b36fa9bdafb8d99613",
};

let app;
if (!getApps().length) {
  app = initializeApp(firebaseConfig);
  console.log('Firebase initialized successfully');
} else {
  app = getApps()[0];
  console.log('Using existing Firebase app');
}

const database = getDatabase(app);

export { database, ref, onValue, set, push };
