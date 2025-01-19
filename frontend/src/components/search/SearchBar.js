import React, { useState, useRef } from 'react'
import './SearchBar.css'
import SearchIcon from '@mui/icons-material/Search'
import useMousePosition from '../../utils/UseMousePosition'

function SearchBar({ placeholder, data }) {
  const [filteredData, setFilteredData] = useState([])
  const searchRef = useRef(null)
  const resultsRef = useRef(null)

  const handleFilter = async (event) => {
    const searchWord = event.target.value

    if (searchWord.length === 0) {
      setFilteredData([])
    } else {
      try {
        const response = await fetch(
          `/search?title=${searchWord}`
        )
        const data = await response.json()
        console.log('API Response:', data)
        setFilteredData(data)
      } catch (error) {
        console.error('Error searching films:', error)
      }
    }
  }

  useMousePosition(searchRef, [filteredData])
  useMousePosition(resultsRef, [filteredData])

  return (
    <div className="search">
      <div className="searchInputs" ref={searchRef}>
        <div className="searchInputs-content">
          <input
            type="text"
            placeholder={placeholder}
            onChange={handleFilter}
          />
          <div className="searchIcon">
            <SearchIcon />
          </div>
        </div>
      </div>
      {filteredData.length !== 0 && (
        <div className="dataResult" ref={resultsRef}>
          <div className="dataResult-content">
            {filteredData.slice(0, 5).map((value, key) => {
              console.log('Mapping value:', value)
              return (
                <a
                  className="dataItem"
                  href={'http://localhost:9001/movie/' + value.title}
                  key={key}
                >
                  <p>{value.title}</p>
                </a>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

export default SearchBar
