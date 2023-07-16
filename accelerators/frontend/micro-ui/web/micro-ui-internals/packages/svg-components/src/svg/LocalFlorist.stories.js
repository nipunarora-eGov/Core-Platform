import React from "react";
import { LocalFlorist } from "./LocalFlorist";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalFlorist",
  component: LocalFlorist,
};

export const Default = () => <LocalFlorist />;
export const Fill = () => <LocalFlorist fill="blue" />;
export const Size = () => <LocalFlorist height="50" width="50" />;
export const CustomStyle = () => <LocalFlorist style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalFlorist className="custom-class" />;
