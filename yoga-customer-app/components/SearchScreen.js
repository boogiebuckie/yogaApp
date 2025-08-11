import React, { useState } from 'react';
import { View, Text, TextInput, Button, StyleSheet, FlatList } from 'react-native';
import { database } from '../firebase.js';
import ClassItem from './ClassItem';

const SearchScreen = () => {
  const [dayOfWeek, setDayOfWeek] = useState('');
  const [timeOfDay, setTimeOfDay] = useState('');
  const [searchResults, setSearchResults] = useState([]);

  const handleSearch = () => {
    const classesRef = database.ref('yoga_courses');
    classesRef.once('value').then((snapshot) => {
      try {
        const data = snapshot.val();
        console.log('SearchScreen Firebase data:', data);
        if (data) {
          const classList = Object.values(data).flatMap(course => Object.values(course));
          console.log('Processed classList:', classList);
          const filteredResults = classList.filter(item => {
            const dayMatch = !dayOfWeek || item.dayOfWeek.toLowerCase() === dayOfWeek.trim().toLowerCase();
            const timeMatch = !timeOfDay || item.time.toLowerCase() === timeOfDay.trim().toLowerCase();
            return dayMatch && timeMatch;
          });
          console.log('Filtered results:', filteredResults);
          setSearchResults(filteredResults);
        } else {
          setSearchResults([]);
          console.log('No data in yoga_courses');
        }
      } catch (error) {
        console.error('SearchScreen error:', error);
      }
    }).catch((error) => {
      console.error('Firebase query error:', error);
    });
  };

  console.log('Search results:', searchResults);

  return (
    <View style={styles.container}>
      <Text>Search for Classes</Text>
      <TextInput
        style={styles.input}
        placeholder="Day of Week"
        value={dayOfWeek}
        onChangeText={setDayOfWeek}
      />
      <TextInput
        style={styles.input}
        placeholder="Time of Day"
        value={timeOfDay}
        onChangeText={setTimeOfDay}
      />
      <Button title="Search" onPress={handleSearch} />
      <FlatList
        data={searchResults}
        renderItem={({ item }) => <ClassItem classData={item} />}
        keyExtractor={(item, index) => index.toString()}
        ListEmptyComponent={<Text>No classes match your search</Text>}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  input: {
    height: 40,
    borderColor: 'gray',
    borderWidth: 1,
    marginBottom: 8,
    paddingHorizontal: 8,
  },
});

export default SearchScreen;