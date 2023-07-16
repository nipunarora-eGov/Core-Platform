import React from "react";
import PropTypes from "prop-types";

export const PausePresentation = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_2161)">
        <path d="M21 19.1H3V5H21V19.1ZM21 3H3C1.9 3 1 3.9 1 5V19C1 20.1 1.9 21 3 21H21C22.1 21 23 20.1 23 19V5C23 3.9 22.1 3 21 3Z" fill={fill} />
        <path d="M9 8H11V16H9V8ZM13 8H15V16H13V8Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_2161">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

