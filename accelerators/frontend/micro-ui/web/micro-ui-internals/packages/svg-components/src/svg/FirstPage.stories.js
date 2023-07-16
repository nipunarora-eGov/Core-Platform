import React from "react";
import { FirstPage } from "./FirstPage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FirstPage",
  component: FirstPage,
};

export const Default = () => <FirstPage />;
export const Fill = () => <FirstPage fill="blue" />;
export const Size = () => <FirstPage height="50" width="50" />;
export const CustomStyle = () => <FirstPage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FirstPage className="custom-class" />;
