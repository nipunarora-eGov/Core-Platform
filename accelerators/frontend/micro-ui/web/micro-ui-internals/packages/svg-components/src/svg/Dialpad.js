import React from "react";
import PropTypes from "prop-types";

export const Dialpad = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2057)">
        <path
          d="M12 19C10.9 19 10 19.9 10 21C10 22.1 10.9 23 12 23C13.1 23 14 22.1 14 21C14 19.9 13.1 19 12 19ZM6 1C4.9 1 4 1.9 4 3C4 4.1 4.9 5 6 5C7.1 5 8 4.1 8 3C8 1.9 7.1 1 6 1ZM6 7C4.9 7 4 7.9 4 9C4 10.1 4.9 11 6 11C7.1 11 8 10.1 8 9C8 7.9 7.1 7 6 7ZM6 13C4.9 13 4 13.9 4 15C4 16.1 4.9 17 6 17C7.1 17 8 16.1 8 15C8 13.9 7.1 13 6 13ZM18 5C19.1 5 20 4.1 20 3C20 1.9 19.1 1 18 1C16.9 1 16 1.9 16 3C16 4.1 16.9 5 18 5ZM12 13C10.9 13 10 13.9 10 15C10 16.1 10.9 17 12 17C13.1 17 14 16.1 14 15C14 13.9 13.1 13 12 13ZM18 13C16.9 13 16 13.9 16 15C16 16.1 16.9 17 18 17C19.1 17 20 16.1 20 15C20 13.9 19.1 13 18 13ZM18 7C16.9 7 16 7.9 16 9C16 10.1 16.9 11 18 11C19.1 11 20 10.1 20 9C20 7.9 19.1 7 18 7ZM12 7C10.9 7 10 7.9 10 9C10 10.1 10.9 11 12 11C13.1 11 14 10.1 14 9C14 7.9 13.1 7 12 7ZM12 1C10.9 1 10 1.9 10 3C10 4.1 10.9 5 12 5C13.1 5 14 4.1 14 3C14 1.9 13.1 1 12 1Z"
          fill={fill}
        />
      </g>
      <defs>
        <clipPath id="clip0_105_2057">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};


