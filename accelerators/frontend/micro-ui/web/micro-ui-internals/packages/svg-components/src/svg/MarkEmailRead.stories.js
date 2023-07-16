import React from "react";
import { MarkEmailRead } from "./MarkEmailRead";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MarkEmailRead",
  component: MarkEmailRead,
};

export const Default = () => <MarkEmailRead />;
export const Fill = () => <MarkEmailRead fill="blue" />;
export const Size = () => <MarkEmailRead height="50" width="50" />;
export const CustomStyle = () => <MarkEmailRead style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MarkEmailRead className="custom-class" />;
