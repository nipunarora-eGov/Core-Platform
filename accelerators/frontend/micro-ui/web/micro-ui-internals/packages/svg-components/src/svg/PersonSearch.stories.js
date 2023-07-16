import React from "react";
import { PersonSearch } from "./PersonSearch";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PersonSearch",
  component: PersonSearch,
};

export const Default = () => <PersonSearch />;
export const Fill = () => <PersonSearch fill="blue" />;
export const Size = () => <PersonSearch height="50" width="50" />;
export const CustomStyle = () => <PersonSearch style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PersonSearch className="custom-class" />;
