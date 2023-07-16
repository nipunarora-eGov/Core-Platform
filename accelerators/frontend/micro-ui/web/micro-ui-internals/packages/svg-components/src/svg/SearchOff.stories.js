import React from "react";
import { SearchOff } from "./SearchOff";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SearchOff",
  component: SearchOff,
};

export const Default = () => <SearchOff />;
export const Fill = () => <SearchOff fill="blue" />;
export const Size = () => <SearchOff height="50" width="50" />;
export const CustomStyle = () => <SearchOff style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SearchOff className="custom-class" />;
