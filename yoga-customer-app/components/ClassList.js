import React, { useEffect, useState } from 'react';
import { database, ref, onValue } from '../firebase'; // adjust path as needed

function ClassList() {
  const [classes, setClasses] = useState([]);

  useEffect(() => {
    const classesRef = ref(database, 'classes');
    const unsubscribe = onValue(classesRef, snapshot => {
      const data = snapshot.val();
      setClasses(data ? Object.values(data) : []);
    });

    return () => unsubscribe();
  }, []);

  return (
    <div>
      {classes.map((cls, idx) => (
        <div key={idx}>{cls.name}</div>
      ))}
    </div>
  );
}

export default ClassList;
