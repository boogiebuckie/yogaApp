import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const ClassItem = ({ classData }) => {
  return (
    <View style={styles.item}>
      <Text style={styles.title}>{classData.typeOfClass}</Text>
      <Text>{classData.dayOfWeek} - {classData.time}</Text>
      <Text>Capacity: {classData.capacity}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  item: {
    backgroundColor: '#f9c2ff',
    padding: 20,
    marginVertical: 8,
  },
  title: {
    fontSize: 24,
  },
});

export default ClassItem;