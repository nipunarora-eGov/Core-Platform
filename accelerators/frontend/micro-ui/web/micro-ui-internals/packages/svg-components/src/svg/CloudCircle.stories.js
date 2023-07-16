import React from "react";
import { CloudCircle } from "./CloudCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "CloudCircle",
  component: CloudCircle,
};

export const Default = () => <CloudCircle />;
export const Fill = () => <CloudCircle fill="blue" />;
export const Size = () => <CloudCircle height="50" width="50" />;
export const CustomStyle = () => <CloudCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <CloudCircle className="custom-class" />;
