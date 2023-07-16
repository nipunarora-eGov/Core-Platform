import React from "react";
import PropTypes from "prop-types";

export const Polymer = ({ className, height = "24", width = "24", style = {}, fill = "#F47738", onClick = null }) => {
  return (
    <svg width={width} height={height} className={className} onClick={onClick} style={style} viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <g clip-path="url(#clip0_105_778)">
        <path d="M19 4H15L7.11 16.63L4.5 12L9 4H5L0.5 12L5 20H9L16.89 7.37L19.5 12L15 20H19L23.5 12L19 4Z" fill={fill} />
      </g>
      <defs>
        <clipPath id="clip0_105_778">
          <rect width="24" height="24" fill="white" />
        </clipPath>
      </defs>
    </svg>
  );
};

