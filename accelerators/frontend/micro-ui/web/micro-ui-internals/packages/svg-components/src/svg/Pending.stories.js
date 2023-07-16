import React from "react";
import { Pending } from "./Pending";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Pending",
  component: Pending,
};

export const Default = () => <Pending />;
export const Fill = () => <Pending fill="blue" />;
export const Size = () => <Pending height="50" width="50" />;
export const CustomStyle = () => <Pending style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Pending className="custom-class" />;
